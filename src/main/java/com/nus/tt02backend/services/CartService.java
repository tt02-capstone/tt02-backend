package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

// ADD TO CART
    public Long addCartItems(String user_type, String tourist_email, String activity_name, List<CartItem> cartItems)
            throws NotFoundException, BadRequestException {

        List<CartBooking> currentCartBookings = getCartBookings(user_type, tourist_email);
        Optional<CartBooking> matchingBooking = findCartBooking(activity_name, cartItems.get(0).getStart_datetime(), currentCartBookings);

        CartBooking updatedBooking;
        if (matchingBooking.isPresent()) {
            updatedBooking = handleExistingCartBooking(matchingBooking.get(), cartItems);
        } else {
            updatedBooking = handleNewCartBooking(activity_name, cartItems);
            currentCartBookings.add(updatedBooking);
        }

        saveCartBookings(user_type, tourist_email, currentCartBookings);

        return updatedBooking.getCart_booking_id();
    }

    private List<CartBooking> getCartBookings(String user_type, String tourist_email) throws BadRequestException {
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
            return currentTourist.getCart_list();
        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            return currentTourist.getCart_list();
        } else {
            throw new BadRequestException("Invalid user type");
        }
    }

    private void saveCartBookings(String user_type, String tourist_email, List<CartBooking> cartBookings) {
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
            currentTourist.setCart_list(cartBookings);
            localRepository.save(currentTourist);
        } else {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            currentTourist.setCart_list(cartBookings);
            touristRepository.save(currentTourist);
        }
    }


    private Optional<CartBooking> findCartBooking(String activity_name, LocalDate startDate, List<CartBooking> currentCartBookings) {
        return currentCartBookings.stream()
                .filter(cartBooking -> cartBooking.getActivity_name().equals(activity_name)
                        && cartBooking.getStart_datetime().equals(startDate.atStartOfDay()))
                .findFirst();
    }

    private CartBooking handleExistingCartBooking(CartBooking existingBooking, List<CartItem> cartItems) throws NotFoundException, BadRequestException {
        List<CartItem> existingCartItems = existingBooking.getCart_item_list();
        for (CartItem cartItem : cartItems) {
            String activitySelection = cartItem.getActivity_selection();
            Optional<CartItem> matchingItem = existingCartItems.stream()
                    .filter(cartItemToCheck -> cartItemToCheck.getActivity_selection().equals(activitySelection))
                    .findFirst();

            // If exists, update
            if (matchingItem.isPresent()) {
                CartItem existingCartItem = matchingItem.get();
                Integer newQuantity = cartItem.getQuantity() + existingCartItem.getQuantity();
                updateCartOperation(existingCartItem.getCart_item_id(), existingBooking.getCart_booking_id(), newQuantity);
            } else {
                // If not exists, add
                List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(
                        existingBooking.getAttraction().getAttraction_id(),
                        cartItems.get(0).getStart_datetime());

                if (currentTickets.isEmpty()) {
                    throw new NotFoundException("No tickets found for this date!");
                }

                cartItem = cartItemRepository.save(cartItem);
                updateTicketCount(activitySelection, cartItem.getQuantity(), currentTickets);

                existingCartItems.add(cartItem);
                existingBooking.setCart_item_list(existingCartItems);
            }

            cartBookingRepository.save(existingBooking);
        }
        return existingBooking;
    }

    private void updateTicketCount(String activitySelection, Integer quantity, List<TicketPerDay> currentTickets) throws NotFoundException {
        OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                .findFirst();

        if (indexOfMatchingTicket.isPresent()) {
            Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
            TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
            currentTicket.setTicket_count(currentTicket.getTicket_count() - quantity);
            ticketPerDayRepository.save(currentTicket);
            currentTickets.set(foundTicketIndex, currentTicket);
        } else {
            throw new NotFoundException("No tickets found for this date!");
        }
    }

    private CartBooking handleNewCartBooking(String activity_name, List<CartItem> cartItems) throws NotFoundException {
        CartBooking cartBookingToCreate = addCartOperation(activity_name, cartItems);

        Attraction selectedAttraction = attractionRepository.getAttractionByName(activity_name);
        List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(
                selectedAttraction.getAttraction_id(),
                cartItems.get(0).getStart_datetime());

        if (currentTickets.isEmpty()) {
            throw new NotFoundException("No tickets found for this date!");
        }

        List<CartItem> addedCartItems = new ArrayList<>();
        for (CartItem cartItemToCreate : cartItems) {
            CartItem newCartItem = cartItemRepository.save(cartItemToCreate);
            addedCartItems.add(newCartItem);

            updateTicketCount(cartItemToCreate.getActivity_selection(), cartItemToCreate.getQuantity(), currentTickets);
        }

        cartBookingToCreate.setCart_item_list(addedCartItems);
        cartBookingRepository.save(cartBookingToCreate);

        return cartBookingToCreate;

    }






    public CartBooking addCartOperation(String activity_name,
                                        List<CartItem> cartItems) throws NotFoundException {

        Attraction selectedAttraction = attractionRepository.getAttractionByName(activity_name);

        LocalDate startDate = cartItems.get(0).getStart_datetime();
        LocalDate endDate = cartItems.get(0).getEnd_datetime();


        // Get current TicketPerDay

        List<TicketPerDay> currentTickets = attractionService.getAllTicketListedByAttractionAndDate(selectedAttraction.getAttraction_id(),
                cartItems.get(0).getStart_datetime()); // tickets listed based on the date selected
        if (currentTickets.isEmpty()) {
            throw new NotFoundException("No tickets found for this date!");
        }

        List<CartItem> addedCartItems = new ArrayList<>();
        for (CartItem cartItemToCreate : cartItems) {

            // [LOG1] To include code to check for existing cartItem


            CartItem newCartItem = cartItemRepository.save(cartItemToCreate);

            addedCartItems.add(newCartItem);

            // Update relevant ticket in TicketPerDay

            String activitySelection = cartItemToCreate.getActivity_selection();

            OptionalInt indexOfMatchingTicket = IntStream.range(0, currentTickets.size())
                    .filter(index -> currentTickets.get(index).getTicket_type().name().equals(activitySelection))
                    .findFirst();

            if (indexOfMatchingTicket.isPresent()) {
                Integer foundTicketIndex = indexOfMatchingTicket.getAsInt();
                TicketPerDay currentTicket = currentTickets.get(foundTicketIndex);
                currentTicket.setTicket_count(currentTicket.getTicket_count() - cartItemToCreate.getQuantity());
                ticketPerDayRepository.save(currentTicket);
                currentTickets.set(foundTicketIndex, currentTicket);

            } else {
                throw new NotFoundException("No tickets found for this date!");

            }

        }

        CartBooking cartBookingToCreate = new CartBooking();
        cartBookingToCreate.setStart_datetime(startDate.atStartOfDay());
        cartBookingToCreate.setEnd_datetime(endDate.atStartOfDay());

        cartBookingToCreate.setType(BookingTypeEnum.ATTRACTION);
        cartBookingToCreate.setActivity_name(selectedAttraction.getName());
        cartBookingToCreate.setAttraction(selectedAttraction);
        cartBookingToCreate.setCart_item_list(addedCartItems);


        cartBookingRepository.save(cartBookingToCreate);


        List<TicketPerDay> updatedList = new ArrayList<>();
        for (TicketPerDay t : currentTickets) {
            updatedList = attractionService.updateTicketsPerDay(selectedAttraction.getAttraction_id(), t);
        }

        selectedAttraction.setTicket_per_day_list(updatedList);
        attractionRepository.save(selectedAttraction);

        return cartBookingToCreate;

    }

    //
    //
    // VIEW CART
    //
    //

    public List<CartBooking> viewCart(String user_type, String tourist_email) throws BadRequestException {



        if (user_type.equals("LOCAL")) {

            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            return currentTourist.getCart_list();


        } else if (user_type.equals("TOURIST")) {

            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);

            return currentTourist.getCart_list();
        } else {
            throw new BadRequestException("Invalid user type");
        }


    }

    //
    //
    // DELETE CART
    //
    //

    public List<Long> deleteCartItems(String user_type, String tourist_email,
                                      List<Long> cart_booking_ids) throws NotFoundException, BadRequestException {

        if (user_type.equals("LOCAL")) {

            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            List<CartBooking> cartBookingsToDelete = deleteCartOperation(cart_booking_ids);

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

            List<CartBooking> cartBookingsToDelete = deleteCartOperation(cart_booking_ids);

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

    public List<CartBooking> deleteCartOperation(List<Long> cart_booking_ids) throws NotFoundException {
        List<CartBooking> cartBookingsToDelete = cartBookingRepository.findCartBookingsByIds(cart_booking_ids);

        for (CartBooking cartBookingToDelete : cartBookingsToDelete) {

            Attraction selected_attraction = cartBookingToDelete.getAttraction();

            List<CartItem> cartItemsToDelete = cartBookingToDelete.getCart_item_list();

            List<TicketPerDay> currentTickets = attractionService.
                    getAllTicketListedByAttractionAndDate(selected_attraction.getAttraction_id(),
                            cartItemsToDelete.get(0).getStart_datetime());
            for (CartItem cartItemToDelete : cartItemsToDelete) {

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
                    throw new NotFoundException("No tickets found for this date!");
                }
            }

            cartBookingToDelete.setCart_item_list(new ArrayList<>());

            cartItemRepository.deleteAll(cartItemsToDelete);

            List<TicketPerDay> updatedList = new ArrayList<>();
            for (TicketPerDay t : currentTickets) {
                updatedList = attractionService.updateTicketsPerDay(selected_attraction.getAttraction_id(), t);
            }

            selected_attraction.setTicket_per_day_list(updatedList);
        }

        return cartBookingsToDelete;
    }

    //
    //
    // UPDATE CART
    //
    //


    public Long updateCartItem(String user_type, String tourist_email, Long cart_item_id, Long cart_booking_id,
                               Integer quantity) throws NotFoundException, BadRequestException {

        if (user_type.equals("LOCAL")) {

            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

            CartBooking cartBooking = updateCartOperation(cart_item_id, cart_booking_id, quantity);

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

            CartBooking cartBooking = updateCartOperation(cart_item_id, cart_booking_id, quantity);

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

    public CartBooking updateCartOperation(Long cart_item_id, Long cart_booking_id,
                                           Integer quantity) throws NotFoundException, BadRequestException {

        Optional<CartBooking> cartBookingOptional = cartBookingRepository.findCartBookingById(cart_booking_id);
        Optional<CartItem> cartItemOptional = cartItemRepository.findCartItemById(cart_item_id);


        if (cartBookingOptional.isPresent() && cartItemOptional.isPresent()) {
            CartBooking cartBooking = cartBookingOptional.get();
            CartItem cartItem = cartItemOptional.get();



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

        } else {
            throw new NotFoundException("No cart item found!");
        }

    }

    //
    //
    // CHECKOUT
    //
    //

    public List<Long> checkout(String user_type, String tourist_email, String payment_method_id,
                               Float totalPrice, List<Long> booking_ids)
            throws StripeException, BadRequestException {

        List<CartBooking> bookingsToCheckout = cartBookingRepository.findCartBookingsByIds(booking_ids);
        BigDecimal totalAmountPayable = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);

        List<Long> createdBookingIds = new ArrayList<>();
        List<Booking> createdBookings = new ArrayList<>();
        if (user_type.equals("LOCAL")) {
            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
            for (CartBooking bookingToCheckout : bookingsToCheckout) {
                Booking createdBooking = processBookingAndPayment(currentTourist, bookingToCheckout, totalAmountPayable, payment_method_id);
                createdBookings.add(createdBooking);
                createdBookingIds.add(createdBooking.getBooking_id());
            }
            updateLocalUser(currentTourist, bookingsToCheckout, createdBookings);
        } else if (user_type.equals("TOURIST")) {
            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
            for (CartBooking bookingToCheckout : bookingsToCheckout) {
                Booking createdBooking = processBookingAndPayment(currentTourist, bookingToCheckout, totalAmountPayable, payment_method_id);
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
            throws StripeException {

        List<BookingItem> bookingItems = createBookingItems(bookingToCheckout);
        Booking newBooking = createBooking(user, bookingToCheckout, bookingItems);
        Payment newPayment = createPayment(newBooking, totalAmountPayable, payment_method_id);
        newBooking.setPayment(newPayment);
        newPayment.setBooking(newBooking);
        bookingRepository.save(newBooking);
        paymentRepository.save(newPayment);
        // Other Payment Logic, if needed
        return newBooking;
    }

    private List<BookingItem> createBookingItems(CartBooking bookingToCheckout) {
        List<BookingItem> bookingItems = new ArrayList<>();
        for (CartItem cartItem : bookingToCheckout.getCart_item_list()) {
            BookingItem newBookingItem = new BookingItem();
            newBookingItem.setQuantity(cartItem.getQuantity());
            newBookingItem.setStart_datetime(cartItem.getStart_datetime());
            newBookingItem.setEnd_datetime(cartItem.getEnd_datetime());
            newBookingItem.setType(cartItem.getType());
            newBookingItem.setActivity_selection(cartItem.getActivity_selection());
            bookingItemRepository.save(newBookingItem);  // Assuming bookingItemRepository is accessible here
            bookingItems.add(newBookingItem);
        }
        return bookingItems;
    }

    private <T> Booking createBooking(T user, CartBooking bookingToCheckout, List<BookingItem> bookingItems) {
        Booking newBooking = new Booking();

        // Populate booking fields that are common for both Local and Tourist
        newBooking.setStart_datetime(bookingToCheckout.getStart_datetime());
        newBooking.setEnd_datetime(bookingToCheckout.getEnd_datetime());
        newBooking.setLast_update(LocalDateTime.now());
        newBooking.setStatus(BookingStatusEnum.UPCOMING);
        newBooking.setType(bookingToCheckout.getType());
        newBooking.setActivity_name(bookingToCheckout.getActivity_name());
        newBooking.setAttraction(bookingToCheckout.getAttraction());
        newBooking.setBooking_item_list(bookingItems);
        newBooking.setQr_code_list(new ArrayList<>());

        // Check user type and populate fields accordingly
        if (user instanceof Local) {
            newBooking.setLocal_user((Local) user);
        } else if (user instanceof Tourist) {
            newBooking.setTourist_user((Tourist) user);
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        // Save the new booking
        bookingRepository.save(newBooking);  // Assuming bookingRepository is accessible here

        return newBooking;
    }

    private Payment createPayment(Booking newBooking, BigDecimal totalAmountPayable, String payment_method_id) throws StripeException {
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

        // Assuming vendorRepository is accessible here
        Vendor vendor = vendorRepository.findVendorByAttractionName(newBooking.getAttraction().getName());
        vendor.setWallet_balance(payoutAmount.add(vendor.getWallet_balance()));

        bookingPayment.setPayment_id(paymentIntent.getId());

        // Assuming paymentRepository is accessible here
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



//    public List<Long> checkout(String user_type, String tourist_email, String payment_method_id,
//                               Float totalPrice,
//                               List<Long> booking_ids) throws StripeException, BadRequestException {
//
//        if (user_type.equals("LOCAL")) {
//
//            Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);
//
//            List<CartBooking> bookingsToCheckout = cartBookingRepository.findCartBookingsByIds(booking_ids);
//
//            BigDecimal totalAmountPayable = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
//
//            List<Payment> processedPayments = new ArrayList<>();
//
//            List<Booking> createdBookings = new ArrayList<>();
//
//            List<Long> createdBookingIds = new ArrayList<>();
//
//
//            for (CartBooking bookingToCheckout : bookingsToCheckout) {
//                BigDecimal amountPayable = BigDecimal.valueOf(0);
//                List<BookingItem> bookingItems = new ArrayList<>();
//                Attraction selected_attraction = bookingToCheckout.getAttraction();
//                for (CartItem cartItem: bookingToCheckout.getCart_item_list()) {
//                    amountPayable = amountPayable.add(cartItem.getPrice());
//                    BookingItem newBookingItem = new BookingItem();
//                    newBookingItem.setQuantity(cartItem.getQuantity());
//                    newBookingItem.setStart_datetime(cartItem.getStart_datetime());
//                    newBookingItem.setEnd_datetime(cartItem.getEnd_datetime());
//                    newBookingItem.setType(cartItem.getType());
//                    newBookingItem.setActivity_selection(cartItem.getActivity_selection());
//                    bookingItemRepository.save(newBookingItem);
//                    bookingItems.add(newBookingItem);
//
//                }
//
//                Booking newBooking = new Booking();
//                newBooking.setStart_datetime(bookingToCheckout.getStart_datetime());
//                newBooking.setEnd_datetime(bookingToCheckout.getEnd_datetime());
//                newBooking.setLast_update(LocalDateTime.now());
//                newBooking.setStatus(BookingStatusEnum.UPCOMING);
//                newBooking.setType(bookingToCheckout.getType());
//                newBooking.setActivity_name(bookingToCheckout.getActivity_name());
//                newBooking.setAttraction(selected_attraction);
//                newBooking.setLocal_user(currentTourist);
//                newBooking.setBooking_item_list(bookingItems);
//                bookingRepository.save(newBooking);
//
//                Vendor vendor = vendorRepository.findVendorByAttractionName(selected_attraction.getName());
//
//                Payment bookingPayment = new Payment();
//                bookingPayment.setPayment_amount(totalAmountPayable);
//                BigDecimal commission = BigDecimal.valueOf(0.10);
//                bookingPayment.setComission_percentage(commission);
//                bookingPayment.setIs_paid(true);
//
//                BigDecimal payoutAmount = totalAmountPayable.subtract(totalAmountPayable.multiply(commission));
//
//                Map<String, Object> automaticPaymentMethods =
//                        new HashMap<>();
//                automaticPaymentMethods.put("enabled", true);
//
//                Map<String, Object> paymentParams = new HashMap<>();
//                paymentParams.put("amount", totalAmountPayable.multiply(new BigDecimal("100")).intValueExact());
//                paymentParams.put("currency", "sgd");
//                paymentParams.put(
//                        "automatic_payment_methods",
//                        automaticPaymentMethods
//                );
//                paymentParams.put(
//                        "confirm",
//                        true
//                );
//                paymentParams.put(
//                        "customer",
//                        currentTourist.getStripe_account_id()
//                );
//
//                paymentParams.put(
//                        "payment_method",
//                        payment_method_id
//                );
//
//                paymentParams.put(
//                        "return_url",
//                        "yourappname://stripe/callback"
//                );
//
//
//                PaymentIntent paymentIntent =
//                        PaymentIntent.create(paymentParams);
//
//                vendor.setWallet_balance(payoutAmount.add(vendor.getWallet_balance()));
//
//                bookingPayment.setPayment_id(paymentIntent.getId());
//                paymentRepository.save(bookingPayment);
//
//
//
//                bookingPayment.setBooking(newBooking);
//                newBooking.setPayment(bookingPayment);
//                bookingRepository.save(newBooking);
//                paymentRepository.save(bookingPayment);
//
//                createdBookings.add(newBooking);
//                createdBookingIds.add(newBooking.getBooking_id());
//                processedPayments.add(bookingPayment);
//            }
//            List<Booking> currentBookings = currentTourist.getBooking_list();
//
//
//            currentBookings.addAll(createdBookings);
//
//            currentTourist.setBooking_list(currentBookings);
//
//
//            List<CartBooking> currentCartBookings = currentTourist.getCart_list();
//
//            currentCartBookings.removeAll(bookingsToCheckout);
//
//            currentTourist.setCart_list(currentCartBookings);
//
//
//            localRepository.save(currentTourist);
//
//
//
//            return createdBookingIds;
//
//        } else if (user_type.equals("TOURIST")) {
//
//            Tourist currentTourist = touristRepository.retrieveTouristByEmail(tourist_email);
//
//            List<CartBooking> bookingsToCheckout = cartBookingRepository.findCartBookingsByIds(booking_ids);
//
//            BigDecimal totalAmountPayable = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
//
//            List<Payment> processedPayments = new ArrayList<>();
//
//            List<Booking> createdBookings = new ArrayList<>();
//
//            List<Long> createdBookingIds = new ArrayList<>();
//
//
//            // Pay vendors?
//
//            for (CartBooking bookingToCheckout : bookingsToCheckout) {
//                BigDecimal amountPayable = BigDecimal.valueOf(0);
//                List<BookingItem> bookingItems = new ArrayList<>();
//                Attraction selected_attraction = bookingToCheckout.getAttraction();
//                for (CartItem cartItem: bookingToCheckout.getCart_item_list()) {
//                    amountPayable = amountPayable.add(cartItem.getPrice());
//                    BookingItem newBookingItem = new BookingItem();
//                    newBookingItem.setQuantity(cartItem.getQuantity());
//                    newBookingItem.setStart_datetime(cartItem.getStart_datetime());
//                    newBookingItem.setEnd_datetime(cartItem.getEnd_datetime());
//                    newBookingItem.setType(cartItem.getType());
//                    newBookingItem.setActivity_selection(cartItem.getActivity_selection());
//                    bookingItemRepository.save(newBookingItem);
//                    bookingItems.add(newBookingItem);
//
//                }
//
//                Booking newBooking = new Booking();
//                newBooking.setStart_datetime(bookingToCheckout.getStart_datetime());
//                newBooking.setEnd_datetime(bookingToCheckout.getEnd_datetime());
//                newBooking.setLast_update(LocalDateTime.now());
//                newBooking.setStatus(BookingStatusEnum.UPCOMING);
//                newBooking.setType(bookingToCheckout.getType());
//                newBooking.setActivity_name(bookingToCheckout.getActivity_name());
//                newBooking.setAttraction(selected_attraction);
//                newBooking.setTourist_user(currentTourist);
//                newBooking.setBooking_item_list(bookingItems);
//                bookingRepository.save(newBooking);
//
//                Vendor vendor = vendorRepository.findVendorByAttractionName(selected_attraction.getName());
//
//                Payment bookingPayment = new Payment();
//                bookingPayment.setPayment_amount(totalAmountPayable);
//                BigDecimal commission = BigDecimal.valueOf(0.10);
//                bookingPayment.setComission_percentage(commission);
//                bookingPayment.setIs_paid(true);
//
//                BigDecimal payoutAmount = totalAmountPayable.subtract(totalAmountPayable.multiply(commission));
//
//
//
//
//
//                Map<String, Object> automaticPaymentMethods =
//                        new HashMap<>();
//                automaticPaymentMethods.put("enabled", true);
//
//                Map<String, Object> paymentParams = new HashMap<>();
//                paymentParams.put("amount", totalAmountPayable.multiply(new BigDecimal("100")).intValueExact());
//                paymentParams.put("currency", "sgd");
//                paymentParams.put(
//                        "automatic_payment_methods",
//                        automaticPaymentMethods
//                );
//                paymentParams.put(
//                        "confirm",
//                        true
//                );
//                paymentParams.put(
//                        "customer",
//                        currentTourist.getStripe_account_id()
//                );
//
//                paymentParams.put(
//                        "payment_method",
//                        payment_method_id
//                );
//
//                paymentParams.put(
//                        "return_url",
//                        "yourappname://stripe/callback"
//                );
//
//
//
//
//                PaymentIntent paymentIntent =
//                        PaymentIntent.create(paymentParams);
//
//                vendor.setWallet_balance(payoutAmount.add(vendor.getWallet_balance()));
//
//                bookingPayment.setPayment_id(paymentIntent.getId());
//                paymentRepository.save(bookingPayment);
//
//
//
//                bookingPayment.setBooking(newBooking);
//                newBooking.setPayment(bookingPayment);
//                bookingRepository.save(newBooking);
//                paymentRepository.save(bookingPayment);
//
//                createdBookings.add(newBooking);
//                createdBookingIds.add(newBooking.getBooking_id());
//                processedPayments.add(bookingPayment);
//            }
//            List<Booking> currentBookings = currentTourist.getBooking_list();
//
//            if (currentBookings == null) {
//                currentBookings = new ArrayList<>(); // Initialize as an empty list if null
//            }
//            currentBookings.addAll(createdBookings);
//            currentTourist.setBooking_list(currentBookings);
//
//
//            List<CartBooking> currentCartBookings = currentTourist.getCart_list();
//
//            currentCartBookings.removeAll(bookingsToCheckout);
//
//            currentTourist.setCart_list(currentCartBookings);
//
//
//            touristRepository.save(currentTourist);
//
//            return createdBookingIds;
//
//
//        } else {
//            throw new BadRequestException("Invalid user type");
//        }
//
//
//    }


}