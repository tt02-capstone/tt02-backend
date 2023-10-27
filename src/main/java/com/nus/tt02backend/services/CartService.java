package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerBalanceTransaction;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Service
public class CartService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingItemRepository bookingItemRepository;
    @Autowired
    CartBookingRepository cartBookingRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    TicketPerDayRepository ticketPerDayRepository;
    @Autowired
    AttractionService attractionService;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    TelecomRepository telecomRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    TourRepository tourRepository;
    @Autowired
    TourTypeRepository tourTypeRepository;
    @Autowired
    DIYEventService diyEventService;
    @Autowired
    ItineraryService itineraryService;

    public Long addCartItems(String user_type, String tourist_email, String activity_name, List<CartItem> cartItems) throws NotFoundException, BadRequestException {

        // Need official function to handle different cases
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            List<CartBooking> currentCartBookings = currentTourist.getCart_list();
            LocalDate startDate = cartItems.get(0).getStart_datetime();
            Optional<CartBooking> matchingBooking = currentCartBookings.stream()
                    .filter(cartBooking -> cartBooking.getActivity_name().equals(activity_name) &&
                            cartBooking.getStart_datetime().equals(startDate.atStartOfDay()))
                    .findFirst();

            OptionalInt indexOpt = IntStream.range(0, currentCartBookings.size())
                    .filter(i -> currentCartBookings.get(i).getActivity_name().equals(activity_name) &&
                            currentCartBookings.get(i).getStart_datetime().equals(startDate.atStartOfDay()))
                    .findFirst();

            if (matchingBooking.isPresent()) {
                CartBooking existingBooking = matchingBooking.get();
                List<CartItem> existingCartItems = existingBooking.getCart_item_list();

                for (CartItem cartItem : cartItems) {
                    String activitySelection = cartItem.getActivity_selection();
                    Optional<CartItem> matchingItem = existingCartItems.stream()
                            .filter(cartItemToCheck -> cartItemToCheck.getActivity_selection().equals(activitySelection))
                            .findFirst();

                    // if exists
                    if (matchingItem.isPresent()) {
                        CartItem existingCartItem = matchingItem.get();
                        Integer newQuantity = cartItem.getQuantity() + existingCartItem.getQuantity();
                        existingBooking = updateCartOperation(existingCartItem.getCart_item_id(), existingBooking.getCart_booking_id(),
                                newQuantity, currentTourist);
                        cartBookingRepository.save(existingBooking);

                    } else {
                        List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(
                                existingBooking.getAttraction().getAttraction_id(),
                                cartItems.get(0).getStart_datetime()); // tickets listed based on the date selected
                        if (currentTickets.isEmpty()) {
                            throw new NotFoundException("No tickets found for this date!");
                        }

                        cartItem = cartItemRepository.save(cartItem);

                        // Update relevant ticket in TicketPerDay
                        OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                                .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                                .findFirst();

                        if (cartItem.getType() != BookingTypeEnum.TOUR) {
                            if (indexOfMatchingTicket.isPresent()) {
                                Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                                TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                                currentTicket.setTicket_count(currentTicket.getTicket_count() - cartItem.getQuantity());
                                ticketPerDayRepository.save(currentTicket);
                                currentTickets.set(foundTicketIndex, currentTicket);

                            } else {
                                throw new NotFoundException("No tickets found for this date!");
                            }
                        }

                        existingCartItems.add(cartItem);
                        existingBooking.setCart_item_list(existingCartItems);
                        cartBookingRepository.save(existingBooking);

                    }
                }

                currentCartBookings.set(indexOpt.getAsInt(), existingBooking);
                currentTourist.setCart_list(currentCartBookings);
                localRepository.save(currentTourist);
                return existingBooking.getCart_booking_id();

            } else {
                CartBooking cartBookingToCreate = addCartOperation(activity_name, cartItems);
                currentCartBookings.add(cartBookingToCreate);
                currentTourist.setCart_list(currentCartBookings);
                localRepository.save(currentTourist);
                return cartBookingToCreate.getCart_booking_id();
            }

        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);

            List<CartBooking> currentCartBookings = currentTourist.getCart_list();
            LocalDate startDate = cartItems.get(0).getStart_datetime();
            Optional<CartBooking> matchingBooking = currentCartBookings.stream()
                    .filter(cartBooking -> cartBooking.getActivity_name().equals(activity_name) &&
                            cartBooking.getStart_datetime().equals(startDate.atStartOfDay()))
                    .findFirst();

            OptionalInt indexOpt = IntStream.range(0, currentCartBookings.size())
                    .filter(i -> currentCartBookings.get(i).getActivity_name().equals(activity_name) &&
                            currentCartBookings.get(i).getStart_datetime().equals(startDate.atStartOfDay()))
                    .findFirst();

            if (matchingBooking.isPresent()) {
                CartBooking existingBooking = matchingBooking.get();

                List<CartItem> existingCartItems = existingBooking.getCart_item_list();

                for (CartItem cartItem : cartItems) {
                    String activitySelection = cartItem.getActivity_selection();
                    Optional<CartItem> matchingItem = existingCartItems.stream()
                            .filter(cartItemToCheck -> cartItemToCheck.getActivity_selection().equals(activitySelection))
                            .findFirst();

                    // if exists
                    if (matchingItem.isPresent()) {
                        CartItem existingCartItem = matchingItem.get();
                        Integer newQuantity = cartItem.getQuantity() + existingCartItem.getQuantity();
                        existingBooking = updateCartOperation(existingCartItem.getCart_item_id(), existingBooking.getCart_booking_id(),
                                newQuantity, currentTourist);

                        cartBookingRepository.save(existingBooking);

                    } else {
                        List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(
                                existingBooking.getAttraction().getAttraction_id(),
                                cartItems.get(0).getStart_datetime()); // tickets listed based on the date selected
                        if (currentTickets.isEmpty()) {
                            throw new NotFoundException("No tickets found for this date that you trying to selected an attraction for !");
                        }

                        cartItem = cartItemRepository.save(cartItem);

                        // Update relevant ticket in TicketPerDay
                        OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                                .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                                .findFirst();

                        if (cartItem.getType() != BookingTypeEnum.TOUR) {
                            if (indexOfMatchingTicket.isPresent()) {
                                Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                                TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                                currentTicket.setTicket_count(currentTicket.getTicket_count() - cartItem.getQuantity());
                                ticketPerDayRepository.save(currentTicket);
                                currentTickets.set(foundTicketIndex, currentTicket);

                            } else {
                                throw new NotFoundException("No tickets found for this date when trying to update ticket count !");
                            }
                        }

//                        if (indexOfMatchingTicket.isPresent()) {
//                            Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
//                            TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
//                            currentTicket.setTicket_count(currentTicket.getTicket_count() - cartItem.getQuantity());
//                            ticketPerDayRepository.save(currentTicket);
//                            currentTickets.set(foundTicketIndex, currentTicket);
//
//                        } else {
//                            throw new NotFoundException("No tickets found for this date when trying to update ticket count !");
//                        }

                        existingCartItems.add(cartItem);
                        existingBooking.setCart_item_list(existingCartItems);
                        cartBookingRepository.save(existingBooking);
                    }
                }

                currentCartBookings.set(indexOpt.getAsInt(), existingBooking);
                currentTourist.setCart_list(currentCartBookings);
                touristRepository.save(currentTourist);
                return existingBooking.getCart_booking_id();

            } else {
                CartBooking cartBookingToCreate = addCartOperation(activity_name, cartItems);
                currentCartBookings.add(cartBookingToCreate);
                currentTourist.setCart_list(currentCartBookings);
                touristRepository.save(currentTourist);
                return cartBookingToCreate.getCart_booking_id();
            }

        } else {
            throw new BadRequestException("Invalid user type");
        }
    }

    public CartBooking addCartOperation(String activity_name, List<CartItem> cartItems) throws NotFoundException {
        Attraction selectedAttraction = attractionRepository.getAttractionByName(activity_name);
        Vendor vendor = vendorRepository.findVendorByAttractionName(activity_name);
        vendor.setVendor_staff_list(null);

        LocalDate startDate = cartItems.get(0).getStart_datetime();
        LocalDate endDate = cartItems.get(0).getEnd_datetime();

        // Get current TicketPerDay
        List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(selectedAttraction.getAttraction_id(),
                cartItems.get(0).getStart_datetime()); // tickets listed based on the date selected
        if (currentTickets.isEmpty()) {
            throw new NotFoundException("No tickets found for this date!");
        }

        List<CartItem> addedCartItems = new ArrayList<>();
        Integer totalTickets = 0;
        for (CartItem cartItemToCreate : cartItems) {
            // [LOG1] To include code to check for existing cartItem
            CartItem newCartItem = cartItemRepository.save(cartItemToCreate);
            addedCartItems.add(newCartItem);

            // Update relevant ticket in TicketPerDay
            String activitySelection = cartItemToCreate.getActivity_selection();

            OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                    .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                    .findFirst();

            if (cartItemToCreate.getType() != BookingTypeEnum.TOUR) {
                if (indexOfMatchingTicket.isPresent()) {
                    Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                    TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                    currentTicket.setTicket_count(currentTicket.getTicket_count() - cartItemToCreate.getQuantity());
                    ticketPerDayRepository.save(currentTicket);
                    currentTickets.set(foundTicketIndex, currentTicket);
                    totalTickets += cartItemToCreate.getQuantity();
                } else {
                    throw new NotFoundException("No tickets found for this date!");
                }
            } else {
                LocalDate tour_date = cartItemToCreate.getStart_datetime();
                String tour_details = cartItemToCreate.getActivity_selection();
                Pattern pattern = Pattern.compile("(.+) \\((.+) - (.+)\\)");

                // Create a Matcher and apply the pattern to the formatted string
                Matcher matcher = pattern.matcher(tour_details);

                if (matcher.matches()) {
                    String selectedTourTypeName = matcher.group(1);
                    String startTimeStr = matcher.group(2);
                    String endTimeStr = matcher.group(3);
                    String[] start_parts = startTimeStr.split(" ");
                    String[] end_parts = endTimeStr.split(" ");
                    startTimeStr = start_parts[0];
                    endTimeStr = end_parts[0];


                    String[] startHourMinute = startTimeStr.split(":");
                    String[] endHourMinute = endTimeStr.split(":");

                    LocalTime startTime = LocalTime.of(Integer.parseInt(startHourMinute[0]), Integer.parseInt(startHourMinute[1]));
                    LocalTime endTime = LocalTime.of(Integer.parseInt(endHourMinute[0]), Integer.parseInt(endHourMinute[1]));
                    if ("PM".equals(start_parts[1])) {
                        if (startTime.getHour() != 12) {
                            startTime = startTime.plusHours(12);
                        }
                    }

                    if ("PM".equals(end_parts[1])) {
                        if (endTime.getHour() != 12) {
                            endTime = endTime.plusHours(12);
                        }
                    }


                    LocalDateTime startDateTime = LocalDateTime.of(tour_date, startTime);
                    LocalDateTime endDateTime = LocalDateTime.of(tour_date, endTime);
                    TourType selected_tourType = tourTypeRepository.findByName(selectedTourTypeName);
                    Tour tour = tourTypeRepository.findTourInTourType(selected_tourType, tour_date.atStartOfDay(), startDateTime, endDateTime);
                    Integer remainder = tour.getRemaining_slot() - totalTickets;
                    if (remainder >= 0) {
                        tour.setRemaining_slot(remainder);
                        tourRepository.save(tour);
                    } else {
                        throw new NotFoundException("Not enough slots for tour");
                    }

                }
            }
        }

        // [LOG1] To check if booking exists
        CartBooking cartBookingToCreate = new CartBooking();
        cartBookingToCreate.setStart_datetime(startDate.atStartOfDay());
        cartBookingToCreate.setEnd_datetime(endDate.atStartOfDay());
        cartBookingToCreate.setType(BookingTypeEnum.ATTRACTION);
        cartBookingToCreate.setActivity_name(selectedAttraction.getName());
        cartBookingToCreate.setAttraction(selectedAttraction);
        cartBookingToCreate.setCart_item_list(addedCartItems);
        cartBookingToCreate.setVendor(vendor);
        cartBookingRepository.save(cartBookingToCreate);

        // Save Attraction
        List<TicketPerDay> updatedList = new ArrayList<>();
        for (TicketPerDay t : currentTickets) {
            updatedList = attractionService.updateTicketsPerDay(selectedAttraction.getAttraction_id(), t);
        }

        selectedAttraction.setTicket_per_day_list(updatedList);
        attractionRepository.save(selectedAttraction);

        return cartBookingToCreate;
    }

    public List<CartBooking> viewCart(String user_type, String tourist_email) throws BadRequestException {
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
            List<CartBooking> cartBookings = currentTourist.getCart_list();
            for (CartBooking i : cartBookings) {
                i.getVendor().setVendor_staff_list(null);
            }
            return cartBookings;

        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            List<CartBooking> cartBookings = currentTourist.getCart_list();
            for (CartBooking i : cartBookings) {
                i.getVendor().setVendor_staff_list(null);
            }
            return cartBookings;
        } else {
            throw new BadRequestException("Invalid user type");
        }
    }

    public List<Long> deleteCartItems(String user_type, String tourist_email, List<Long> cart_booking_ids) throws NotFoundException, BadRequestException {
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            List<CartBooking> cartBookingsToDelete = deleteCartOperation(cart_booking_ids, currentTourist);

            currentTourist.getCart_list().removeAll(cartBookingsToDelete);

            cartBookingRepository.deleteAll(cartBookingsToDelete);

            List<CartBooking> updatedCartBookings = currentTourist.getCart_list()
                    .stream()
                    .filter(cart -> !cart_booking_ids.contains(cart.getCart_booking_id()))
                    .toList();

            currentTourist.setCart_list(updatedCartBookings);

            localRepository.save(currentTourist);
            cartBookingRepository.deleteAll(cartBookingsToDelete);
            return cart_booking_ids;

        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            List<CartBooking> cartBookingsToDelete = deleteCartOperation(cart_booking_ids, currentTourist);

            List<CartBooking> updatedCartBookings = currentTourist.getCart_list()
                    .stream()
                    .filter(cart -> !cart_booking_ids.contains(cart.getCart_booking_id()))
                    .toList();


            currentTourist.setCart_list(updatedCartBookings);
            cartBookingRepository.deleteAll(cartBookingsToDelete);
            touristRepository.save(currentTourist);
            cartBookingRepository.deleteAll(cartBookingsToDelete);

            return cart_booking_ids;
        } else {
            throw new BadRequestException("Invalid user type");
        }
    }

    public List<CartBooking> deleteCartOperation(List<Long> cart_booking_ids, User user) throws NotFoundException {
        List<CartBooking> cartBookingsToDelete = cartBookingRepository.findCartBookingsByIds(cart_booking_ids);

        for (CartBooking cartBookingToDelete : cartBookingsToDelete) {

            if (cartBookingToDelete.getType() == BookingTypeEnum.ATTRACTION) {

                Attraction selected_attraction = cartBookingToDelete.getAttraction();

                List<CartItem> cartItemsToDelete = cartBookingToDelete.getCart_item_list();

                List<TicketPerDay> currentTickets = attractionService.
                        getAllTicketListedByAttractionAndDate(selected_attraction.getAttraction_id(),
                                cartItemsToDelete.get(0).getStart_datetime());

                for (CartItem cartItemToDelete : cartItemsToDelete) {
                    if (cartItemToDelete.getType() == BookingTypeEnum.ATTRACTION) { // to cater to deleting of tours as tours do not have tickets donnid to do indexmatching
                        String activitySelection = cartItemToDelete.getActivity_selection();
                        OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                                .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                                .findFirst();

                        if (indexOfMatchingTicket.isPresent()) {
                            Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                            TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                            currentTicket.setTicket_count(currentTicket.getTicket_count() + cartItemToDelete.getQuantity());
                            ticketPerDayRepository.save(currentTicket);
                            currentTickets.set(foundTicketIndex, currentTicket);

                        } else {
                            throw new NotFoundException("No tickets found for this date when deleting items from cart!");
                        }
                    }
                }

                cartBookingToDelete.setCart_item_list(new ArrayList<>());
                cartItemRepository.deleteAll(cartItemsToDelete);

                List<TicketPerDay> updatedList = new ArrayList<>();
                for (TicketPerDay t : currentTickets) {
                    updatedList = attractionService.updateTicketsPerDay(selected_attraction.getAttraction_id(), t);
                }

                selected_attraction.setTicket_per_day_list(updatedList);

            } else if (cartBookingToDelete.getType() == BookingTypeEnum.TELECOM) {
                if (user instanceof Tourist) {
                    Tourist tourist = (Tourist) user;
                    tourist.getCart_list().remove(cartBookingToDelete);
                } else {
                    Local local = (Local) user;
                    local.getCart_list().remove(cartBookingToDelete);
                }
                CartItem temp = cartBookingToDelete.getCart_item_list().get(0);
                cartBookingToDelete.getCart_item_list().clear();
                cartItemRepository.delete(temp);
                cartBookingRepository.delete(cartBookingToDelete);
            // might need to edit more
            } else if (cartBookingToDelete.getType() == BookingTypeEnum.ACCOMMODATION) {
            if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                tourist.getCart_list().remove(cartBookingToDelete);
            } else {
                Local local = (Local) user;
                local.getCart_list().remove(cartBookingToDelete);
            }
            CartItem temp = cartBookingToDelete.getCart_item_list().get(0);
            cartBookingToDelete.getCart_item_list().clear();
            cartItemRepository.delete(temp);
            cartBookingRepository.delete(cartBookingToDelete);
        }
        }

        return cartBookingsToDelete;
    }

    public Long updateCartItem(String user_type, String tourist_email, Long cart_item_id, Long cart_booking_id, Integer quantity) throws NotFoundException, BadRequestException {
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            CartBooking cartBooking = updateCartOperation(cart_item_id, cart_booking_id, quantity, currentTourist);

            List<CartBooking> cartBookingsToUpdate = currentTourist.getCart_list();

            OptionalInt indexCartBooking = IntStream.range(0, cartBookingsToUpdate.size())
                    .filter(i -> Objects.equals(cartBookingsToUpdate.get(i).getCart_booking_id(), cartBooking.getCart_booking_id()))
                    .findFirst();

            if (cartBooking.getCart_item_list().isEmpty()) {
                cartBookingsToUpdate.remove(indexCartBooking.getAsInt());
                cartBookingRepository.delete(cartBooking);
            } else {
                cartBookingsToUpdate.set(indexCartBooking.getAsInt(), cartBooking);
                currentTourist.setCart_list(cartBookingsToUpdate);
                localRepository.save(currentTourist);
            }

        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);

            CartBooking cartBooking = updateCartOperation(cart_item_id, cart_booking_id, quantity, currentTourist);

            List<CartBooking> cartBookingsToUpdate = currentTourist.getCart_list();

            OptionalInt indexCartBooking = IntStream.range(0, cartBookingsToUpdate.size())
                    .filter(i -> Objects.equals(cartBookingsToUpdate.get(i).getCart_booking_id(), cartBooking.getCart_booking_id()))
                    .findFirst();

            if (cartBooking.getCart_item_list().isEmpty()) {
                cartBookingsToUpdate.remove(indexCartBooking.getAsInt());
                cartBookingRepository.delete(cartBooking);

            } else {
                cartBookingsToUpdate.set(indexCartBooking.getAsInt(), cartBooking);
                currentTourist.setCart_list(cartBookingsToUpdate);
                touristRepository.save(currentTourist);
            }

        } else {
            throw new BadRequestException("Invalid user type");
        }

        return cart_item_id;
    }

    public CartBooking updateCartOperation(Long cart_item_id, Long cart_booking_id, Integer quantity, User user) throws NotFoundException, BadRequestException {

        Optional<CartBooking> cartBookingOptional = cartBookingRepository.findCartBookingById(cart_booking_id);
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemById(cart_item_id);

        if (cartBookingOptional.isPresent() && cartItemOptional.isPresent()) {
            CartBooking cartBooking = cartBookingOptional.get();
            CartItem cartItem = cartItemOptional.get();

            if (cartBooking.getType() == BookingTypeEnum.ATTRACTION) {
                Attraction selected_attraction = cartBooking.getAttraction();

                List<TicketPerDay> currentTickets = attractionService.
                        getAllTicketListedByAttractionAndDate(selected_attraction.getAttraction_id(),
                                LocalDate.from(cartBooking.getStart_datetime()));

                String activitySelection = cartItem.getActivity_selection();

                OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                        .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                        .findFirst();

                Integer changeInQuantity = quantity - cartItem.getQuantity();

                if (indexOfMatchingTicket.isPresent()) {
                    Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                    TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                    Integer updatedTicketCount = currentTicket.getTicket_count() + changeInQuantity;
                    if (updatedTicketCount > 0) {
                        currentTicket.setTicket_count(updatedTicketCount);
                        ticketPerDayRepository.save(currentTicket);
                        currentTickets.set(foundTicketIndex, currentTicket);
                    } else {
                        throw new BadRequestException("Insufficient Inventory for Ticket Type: " +
                                currentTicket.getTicket_type());
                    }
                } else {
                    throw new NotFoundException("No tickets found for this date!");
                }

                List<TicketPerDay> updatedList = new ArrayList<>();
                for (TicketPerDay t : currentTickets) {
                    updatedList = attractionService.updateTicketsPerDay(selected_attraction.getAttraction_id(), t);
                }

                selected_attraction.setTicket_per_day_list(updatedList);

                List<CartItem> cartItemsToUpdate = cartBooking.getCart_item_list();

                OptionalInt indexOpt = IntStream.range(0, cartItemsToUpdate.size())
                        .filter(i -> Objects.equals(cartItemsToUpdate.get(i).getCart_item_id(), cartItem.getCart_item_id()))
                        .findFirst();

                if (quantity <= 0) {
                    cartItemsToUpdate.remove(indexOpt.getAsInt());
                    cartItemRepository.delete(cartItem);

                } else {
                    cartItem.setQuantity(quantity);
                    cartItemRepository.save(cartItem);
                    cartItemsToUpdate.set(indexOpt.getAsInt(), cartItem); // To check for present
                }

                cartBooking.setCart_item_list(cartItemsToUpdate);
                cartBookingRepository.save(cartBooking);
                return cartBooking;

            } else if (cartBooking.getType() == BookingTypeEnum.TELECOM) {
                if (quantity < 0) {
                    throw new BadRequestException("Cart item quantity cannot be negative!");

                } else if (quantity == 0) {
                    if (user instanceof Local) {
                        Local local = (Local) user;
                        local.getCart_list().remove(cartBooking);
                    } else {
                        Tourist tourist = (Tourist) user;
                        tourist.getCart_list().remove(cartBooking);
                    }
                    cartBooking.getCart_item_list().clear();
                    cartItemRepository.delete(cartItem);
                    cartBookingRepository.delete(cartBooking);

                } else {
                    cartItem.setQuantity(quantity);
                    cartItemRepository.save(cartItem);
                    cartBookingRepository.save(cartBooking);
                }

                return cartBooking;

            } else {
                throw new BadRequestException("Yet to implement"); // to be changed once tour and accom comes in
            }
        } else {
            throw new NotFoundException("No cart item found!");
        }
    }

    public Long addTelecomToCart(Long userId, Long telecomId, CartBooking cartBooking) throws NotFoundException {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));
        Telecom telecom = telecomRepository.findById(telecomId).orElseThrow(() -> new NotFoundException("Telecom not found!"));
        Vendor vendor = vendorRepository.findVendorByTelecomId(telecomId);

        List<CartItem> list = cartBooking.getCart_item_list();
        for (CartItem c : list) {
            cartItemRepository.save(c);
        }

        cartBooking.setTelecom(telecom);
        vendor.setVendor_staff_list(null);
        cartBooking.setVendor(vendor);
        cartBookingRepository.save(cartBooking);

        if (user instanceof Local) {
            Local local = (Local) user;
            if (local.getCart_list() == null) local.setCart_list(new ArrayList<>());
            local.getCart_list().add(cartBooking);
            localRepository.save(local);
            return cartBooking.getCart_booking_id();

        } else if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            if (tourist.getCart_list() == null) tourist.setCart_list(new ArrayList<>());
            tourist.getCart_list().add(cartBooking);
            touristRepository.save(tourist);
            return cartBooking.getCart_booking_id();

        } else {
            throw new NotFoundException("User is not tourist or local!");
        }
    }

    public Long addRoomToCart(Long userId, Long roomId, CartBooking cartBooking) throws NotFoundException {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found!"));
        Vendor vendor = vendorRepository.findVendorByRoomId(roomId);

        List<CartItem> list = cartBooking.getCart_item_list();
        for (CartItem c : list) {
            cartItemRepository.save(c);
        }

        cartBooking.setRoom(room);
        vendor.setVendor_staff_list(null);
        cartBooking.setVendor(vendor);
        cartBookingRepository.save(cartBooking);

        if (user instanceof Local) {
            Local local = (Local) user;
            if (local.getCart_list() == null) local.setCart_list(new ArrayList<>());
            local.getCart_list().add(cartBooking);
            localRepository.save(local);
            return cartBooking.getCart_booking_id();

        } else if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            if (tourist.getCart_list() == null) tourist.setCart_list(new ArrayList<>());
            tourist.getCart_list().add(cartBooking);
            touristRepository.save(tourist);
            return cartBooking.getCart_booking_id();

        } else {
            throw new NotFoundException("User is not tourist or local!");
        }
    }

    public Long addTourToCart(Long userId, Long tourId, CartBooking cartBooking) throws NotFoundException {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found!"));
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new NotFoundException("Tour not found!"));

        List<CartItem> list = cartBooking.getCart_item_list();
        for (CartItem c : list) {
            cartItemRepository.save(c);
        }

        cartBooking.setTour(tour);
        cartBookingRepository.save(cartBooking);

        if (user instanceof Local) {
            Local local = (Local) user;
            if (local.getCart_list() == null) local.setCart_list(new ArrayList<>());
            local.getCart_list().add(cartBooking);
            localRepository.save(local);
            return cartBooking.getCart_booking_id();

        } else if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            if (tourist.getCart_list() == null) tourist.setCart_list(new ArrayList<>());
            tourist.getCart_list().add(cartBooking);
            touristRepository.save(tourist);
            return cartBooking.getCart_booking_id();

        } else {
            throw new NotFoundException("User is not tourist or local!");
        }
    }


    public List<Long> checkout(String user_type, String tourist_email, String payment_method_id, Float totalPrice, List<Long> booking_ids, List<BigDecimal> priceList)
            throws StripeException, BadRequestException, NotFoundException {

        // Should fetch via User if possible
        List<CartBooking> bookingsToCheckout = cartBookingRepository.findCartBookingsByIds(booking_ids);
        int index = 0;
        CartBooking cartBookingToCreate = null;
        for (CartBooking booking : bookingsToCheckout) {

            if ("ATTRACTION".equals(String.valueOf(booking.getType()))) {
                // Loop through cart items for the current booking to check for tours

                Optional<CartItem> optionalCartItem = booking.getCart_item_list().stream()
                        .filter(bookingItem -> "TOUR".equals(String.valueOf(bookingItem.getType())))
                        .findFirst();

                if (optionalCartItem.isPresent()) {
                    CartItem tour_booking = optionalCartItem.get();


                    List<CartItem> cartItems = new ArrayList<>();
                    cartItems.add(tour_booking);

                    LocalDate tour_date = tour_booking.getStart_datetime();
                    String tour_details = tour_booking.getActivity_selection();
                    Pattern pattern = Pattern.compile("(.+) \\((.+) - (.+)\\)");

                    // Create a Matcher and apply the pattern to the formatted string
                    Matcher matcher = pattern.matcher(tour_details);

                    if (matcher.matches()) {
                        String selectedTourTypeName = matcher.group(1);
                        String startTimeStr = matcher.group(2);
                        String endTimeStr = matcher.group(3);
                        String[] start_parts = startTimeStr.split(" ");
                        String[] end_parts = endTimeStr.split(" ");
                        startTimeStr = start_parts[0];
                        endTimeStr = end_parts[0];


                        String[] startHourMinute = startTimeStr.split(":");
                        String[] endHourMinute = endTimeStr.split(":");

                        LocalTime startTime = LocalTime.of(Integer.parseInt(startHourMinute[0]), Integer.parseInt(startHourMinute[1]));
                        LocalTime endTime = LocalTime.of(Integer.parseInt(endHourMinute[0]), Integer.parseInt(endHourMinute[1]));
                        if ("PM".equals(start_parts[1])) {
                            if (startTime.getHour() != 12) {
                                startTime = startTime.plusHours(12);
                            }
                        }

                        if ("PM".equals(end_parts[1])) {
                            if (endTime.getHour() != 12) {
                                endTime = endTime.plusHours(12);
                            }
                        }


                        LocalDateTime startDateTime = LocalDateTime.of(tour_date, startTime);
                        LocalDateTime endDateTime = LocalDateTime.of(tour_date, endTime);
                        System.out.println(startDateTime);
                        System.out.println(endDateTime);
                        System.out.println(tour_date.atStartOfDay());
                        TourType selected_tourType = tourTypeRepository.findByName(selectedTourTypeName);
                        System.out.println(selected_tourType.getTour_type_id());
                        Tour tour = tourTypeRepository.findTourInTourType(selected_tourType, tour_date.atStartOfDay(), startDateTime, endDateTime);
                        System.out.println(tour.getTour_id());
                        cartBookingToCreate = new CartBooking();
                        cartBookingToCreate.setStart_datetime(startDateTime);
                        cartBookingToCreate.setEnd_datetime(endDateTime);
                        cartBookingToCreate.setType(BookingTypeEnum.TOUR);
                        cartBookingToCreate.setActivity_name(selectedTourTypeName);
                        cartBookingToCreate.setTour(tour);
                        cartBookingToCreate.setCart_item_list(cartItems);

                        BigDecimal tour_rawtotal = tour_booking.getPrice().multiply(BigDecimal.valueOf(tour_booking.getQuantity()));

                        BigDecimal attraction_rawtotal = BigDecimal.ZERO;

                        for (CartItem item : booking.getCart_item_list()) {
                            if (!("TOUR".equals(String.valueOf(item.getType())))) {
                                attraction_rawtotal = attraction_rawtotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                            }

                        }


                        BigDecimal raw_total = tour_rawtotal.add(attraction_rawtotal);

                        BigDecimal discounted_total = priceList.get(index);

                        BigDecimal attraction_subtotal = BigDecimal.ZERO;

                        BigDecimal tour_subtotal = BigDecimal.ZERO;

                        if (!(raw_total.equals(discounted_total))) {

                            BigDecimal difference = raw_total.subtract(discounted_total);

                            BigDecimal rate = difference.divide(raw_total, 4, RoundingMode.HALF_UP);

                            attraction_subtotal = attraction_rawtotal.subtract(attraction_rawtotal.multiply(rate));
                            tour_subtotal = tour_rawtotal.subtract(tour_rawtotal.multiply(rate));


                        } else {
                            attraction_subtotal = attraction_rawtotal;
                            tour_subtotal = tour_rawtotal;
                        }



                        priceList.set(index, attraction_subtotal.setScale(2, RoundingMode.HALF_UP));

                        priceList.add(tour_subtotal.setScale(2, RoundingMode.HALF_UP));

                    }
                }

            }

            index++;
        }


        if (cartBookingToCreate != null) {
            bookingsToCheckout.add(cartBookingToCreate);
        }




        Map<Long, BigDecimal> map = new HashMap<>();
        for (int i = 0; i < bookingsToCheckout.size(); i++) {
            map.put(bookingsToCheckout.get(i).getCart_booking_id(), priceList.get(i));
        }

        List<Long> createdBookingIds = new ArrayList<>();
        List<Booking> createdBookings = new ArrayList<>();
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
            for (CartBooking bookingToCheckout : bookingsToCheckout) {
                BigDecimal totalAmountPayable = map.get(bookingToCheckout.getCart_booking_id()).setScale(2, RoundingMode.HALF_UP);
                Booking createdBooking = processBookingAndPayment(currentTourist, bookingToCheckout, totalAmountPayable, payment_method_id);
                createdBooking.setBooked_user(UserTypeEnum.LOCAL);
                createdBookings.add(createdBooking);
                createdBookingIds.add(createdBooking.getBooking_id());
            }
            updateLocalUser(currentTourist, bookingsToCheckout, createdBookings);
        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            for (CartBooking bookingToCheckout : bookingsToCheckout) {
                BigDecimal totalAmountPayable = map.get(bookingToCheckout.getCart_booking_id()).setScale(2, RoundingMode.HALF_UP);
                Booking createdBooking = processBookingAndPayment(currentTourist, bookingToCheckout, totalAmountPayable, payment_method_id);
                createdBooking.setBooked_user(UserTypeEnum.TOURIST);
                createdBookings.add(createdBooking);
                createdBookingIds.add(createdBooking.getBooking_id());
            }
            updateTouristUser(currentTourist, bookingsToCheckout, createdBookings);
        } else {
            throw new BadRequestException("Invalid user type");
        }

        return createdBookingIds;
    }

    private <T> Booking processBookingAndPayment(T user, CartBooking bookingToCheckout, BigDecimal totalAmountPayable, String payment_method_id)
            throws StripeException, NotFoundException, BadRequestException {

        List<BookingItem> bookingItems = createBookingItems(bookingToCheckout);
        Booking newBooking = createBooking(user, bookingToCheckout, bookingItems);
        Payment newPayment = createPayment(newBooking, totalAmountPayable, payment_method_id);
        newBooking.setPayment(newPayment);
        newPayment.setBooking(newBooking);
        Booking savedBooking = bookingRepository.save(newBooking);
        paymentRepository.save(newPayment);

        // create a diy booking event
        this.createBookingDIYEvent((User) user, savedBooking);

        return newBooking;
    }

    private List<BookingItem> createBookingItems(CartBooking bookingToCheckout) {
        List<BookingItem> bookingItems = new ArrayList<>();
        for (CartItem cartItem : bookingToCheckout.getCart_item_list()) {
            if (!(Objects.equals(String.valueOf(bookingToCheckout.getType()), "ATTRACTION") && Objects.equals(String.valueOf(cartItem.getType()), "TOUR"))) {
                BookingItem newBookingItem = new BookingItem();
                newBookingItem.setQuantity(cartItem.getQuantity());
                newBookingItem.setStart_datetime(cartItem.getStart_datetime());
                newBookingItem.setEnd_datetime(cartItem.getEnd_datetime());
                newBookingItem.setType(cartItem.getType());
                newBookingItem.setActivity_selection(cartItem.getActivity_selection());
                bookingItemRepository.save(newBookingItem);
                bookingItems.add(newBookingItem);
            }

        }
        return bookingItems;
    }

    private <T> Booking createBooking(T user, CartBooking bookingToCheckout, List<BookingItem> bookingItems) throws BadRequestException {
        Booking newBooking = new Booking();

        // Populate booking fields that are common for both Local and Tourist
        newBooking.setStart_datetime(bookingToCheckout.getStart_datetime());
        newBooking.setEnd_datetime(bookingToCheckout.getEnd_datetime());
        newBooking.setLast_update(LocalDateTime.now());
        newBooking.setStatus(BookingStatusEnum.UPCOMING);
        newBooking.setType(bookingToCheckout.getType());
        newBooking.setActivity_name(bookingToCheckout.getActivity_name());
        String activity_type = String.valueOf(bookingToCheckout.getType());
        //ACCOMODATION, TELECOM, ATTRACTION, TOUR


        if (Objects.equals(activity_type, "ATTRACTION")) {
            newBooking.setAttraction(bookingToCheckout.getAttraction());

        } else if (Objects.equals(activity_type, "TELECOM")) {
            newBooking.setTelecom(bookingToCheckout.getTelecom());
        } else if (Objects.equals(activity_type, "ACCOMMODATION")) {
            newBooking.setRoom(bookingToCheckout.getRoom());
        }   else if (Objects.equals(activity_type, "TOUR")) {
            newBooking.setTour(bookingToCheckout.getTour());
        }

        newBooking.setBooking_item_list(bookingItems);
        newBooking.setQr_code_list(new ArrayList<>());

        // Check user type and populate fields accordingly
        if (user instanceof Local) {
            Local local = (Local) user;
            newBooking.setLocal_user(local);
        } else if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            newBooking.setTourist_user(tourist);
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        // Save the new booking
        bookingRepository.save(newBooking);
        return newBooking;
    }

    private Payment createPayment(Booking newBooking, BigDecimal totalAmountPayable, String payment_method_id) throws StripeException, NotFoundException {
        Payment bookingPayment = new Payment();
        bookingPayment.setPayment_amount(totalAmountPayable);

        // Assuming a 10% commission for the example
        BigDecimal commission = BigDecimal.valueOf(0.10);
        bookingPayment.setComission_percentage(commission);
        bookingPayment.setIs_paid(true);

        BigDecimal payoutAmount = totalAmountPayable.subtract(totalAmountPayable.multiply(commission));

        Map<String, Object> automaticPaymentMethods = new HashMap<>();
        automaticPaymentMethods.put("enabled", true);

        Map<String, Object> paymentParams = new HashMap<>();
        paymentParams.put("amount", totalAmountPayable.multiply(new BigDecimal("100")).intValueExact());
        paymentParams.put("currency", "sgd");
        paymentParams.put("automatic_payment_methods", automaticPaymentMethods);
        paymentParams.put("confirm", true);

        String stripeAccountId = (newBooking.getLocal_user() != null) ? newBooking.getLocal_user().getStripe_account_id() :
                (newBooking.getTourist_user() != null) ? newBooking.getTourist_user().getStripe_account_id() :
                        null;

        if(stripeAccountId == null) {
            throw new IllegalStateException("No Stripe account ID found for the booking user.");
        }

        paymentParams.put("customer", stripeAccountId);
        paymentParams.put("payment_method", payment_method_id);
        paymentParams.put("return_url", "yourappname://stripe/callback");

        PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);

        String activity_type = String.valueOf(newBooking.getType());
        Vendor vendor = null;
        Local local = null;

        if (Objects.equals(activity_type, "TOUR")) {
            local = localRepository.findLocalByTour(newBooking.getTour());
            if (local != null) {
                local.setWallet_balance(payoutAmount.add(local.getWallet_balance()));
                String stripe_account_id = local.getStripe_account_id();

                Customer customer =
                        Customer.retrieve(stripe_account_id);

                Map<String, Object> params = new HashMap<>();
                params.put("amount", payoutAmount.multiply(new BigDecimal("100")).intValueExact());
                params.put("currency", "sgd");
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_type", "Earnings");
                params.put("metadata", metadata);

                CustomerBalanceTransaction balanceTransaction =
                        customer.balanceTransactions().create(params);
            } else {
                throw new NotFoundException("No locals found associated with tour");
            }

        } else {
            if (Objects.equals(activity_type, "ATTRACTION")) {
                vendor = vendorRepository.findVendorByAttractionName(newBooking.getAttraction().getName());

            } else if (Objects.equals(activity_type, "TELECOM")) {
                vendor = vendorRepository.findVendorByTelecomName(newBooking.getTelecom().getName());

            } else if (Objects.equals(activity_type, "ACCOMMODATION")) {
                vendor = vendorRepository.findVendorByAccommodationName(newBooking.getActivity_name());
            }

            if (!(vendor == null)) {
                vendor.setWallet_balance(payoutAmount.add(vendor.getWallet_balance()));
                String stripe_account_id = vendor.getStripe_account_id();

                Customer customer =
                        Customer.retrieve(stripe_account_id);

                Map<String, Object> params = new HashMap<>();
                params.put("amount", payoutAmount.multiply(new BigDecimal("100")).intValueExact());
                params.put("currency", "sgd");
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_type", "Earnings");
                params.put("metadata", metadata);

                CustomerBalanceTransaction balanceTransaction =
                        customer.balanceTransactions().create(params);
            }
        }



        bookingPayment.setPayment_id(paymentIntent.getId());


        paymentRepository.save(bookingPayment);

        return bookingPayment;
    }

    private void updateLocalUser(Local currentTourist, List<CartBooking> bookingsToCheckout, List<Booking> createdBookings) {
        // Add the newly created bookings to the Local user's list of bookings
        List<Booking> currentBookings = currentTourist.getBooking_list();
        if (currentBookings == null) {
            currentBookings = new ArrayList<>(); // Initialize as an empty list if null
        }
        currentBookings.addAll(createdBookings);
        currentTourist.setBooking_list(currentBookings);

        // Remove the checked-out cart bookings from the Local user's cart
        List<CartBooking> currentCartBookings = currentTourist.getCart_list();
        currentCartBookings.removeAll(bookingsToCheckout);
        currentTourist.setCart_list(currentCartBookings);

        // Assuming localRepository is accessible here
        localRepository.save(currentTourist);
    }

    private void updateTouristUser(Tourist currentTourist, List<CartBooking> bookingsToCheckout , List<Booking> createdBookings) {
        // Add the newly created bookings to the Tourist user's list of bookings
        List<Booking> currentBookings = currentTourist.getBooking_list();
        if (currentBookings == null) {
            currentBookings = new ArrayList<>(); // Initialize as an empty list if null
        }
        currentBookings.addAll(createdBookings);
        currentTourist.setBooking_list(currentBookings);

        // Remove the checked-out cart bookings from the Tourist user's cart
        List<CartBooking> currentCartBookings = currentTourist.getCart_list();
        currentCartBookings.removeAll(bookingsToCheckout);
        currentTourist.setCart_list(currentCartBookings);

        // Assuming touristRepository is accessible here
        touristRepository.save(currentTourist);
    }

    // create a new itinerary event for the booking
    private void createBookingDIYEvent(User user, Booking booking) throws BadRequestException {
        Itinerary itinerary = itineraryService.getItineraryByUser(((User) user).getUser_id());
        if (itinerary != null && withinItineraryDates(itinerary, booking)) {
            Long itineraryId = itinerary.getItinerary_id();
            DIYEvent diyEvent = new DIYEvent();
            diyEvent.setName(booking.getActivity_name());
            diyEvent.setStart_datetime(booking.getStart_datetime());
            diyEvent.setEnd_datetime(booking.getEnd_datetime());
            diyEvent.setLocation("");
            diyEvent.setRemarks("");
            diyEventService.createDiyEvent(itineraryId, booking.getBooking_id(), "booking", diyEvent);
        }
    }

    public boolean withinItineraryDates(Itinerary itinerary, Booking booking) {
        if (booking.getTelecom() != null || booking.getRoom() != null) {
            return booking.getStart_datetime().isAfter(itinerary.getStart_date()) && booking.getStart_datetime().isBefore(itinerary.getEnd_date());
        } else { // attraction
            return booking.getStart_datetime().isAfter(itinerary.getStart_date()) && booking.getStart_datetime().isBefore(itinerary.getEnd_date()) &&
                   booking.getEnd_datetime().isAfter(itinerary.getStart_date()) && booking.getEnd_datetime().isBefore(itinerary.getEnd_date());
        }
    }
}