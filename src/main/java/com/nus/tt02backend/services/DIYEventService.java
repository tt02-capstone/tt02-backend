package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.*;


@Service
public class DIYEventService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    ItineraryRepository itineraryRepository;
    @Autowired
    DIYEventRepository diyEventRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    AccommodationRepository accommodationRepository;
    @Autowired
    TelecomRepository telecomRepository;
    @Autowired
    RestaurantRepository restaurantRepository;
    @Autowired
    BookingRepository bookingRepository;

    public List<DIYEvent> getAllDiyEvents(Long itineraryId) throws NotFoundException, BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> diyEvents = itinerary.getDiy_event_list();

        for (DIYEvent d : diyEvents) {
            if (d.getBooking() != null) {
                d.getBooking().setPayment(null);
                d.getBooking().setLocal_user(null);
                d.getBooking().setTourist_user(null);
            }
        }

        return diyEvents;
    }

    public DIYEvent getDiyEvent(Long diyEventId) throws NotFoundException {
        try {
            Optional<DIYEvent> diyEventOptional = diyEventRepository.findById(diyEventId);
            if (diyEventOptional.isPresent()) {
                DIYEvent diyEvent = diyEventOptional.get();

                if (diyEvent.getBooking() != null) {
                    diyEvent.getBooking().getPayment().setBooking(null);
                    diyEvent.getBooking().setTourist_user(null);
                    diyEvent.getBooking().setLocal_user(null);
                }

                return diyEvent;
            } else {
                throw new NotFoundException("DIY Event not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<DIYEvent> getAllDiyEventsByDay(Long itineraryId, Long dayNumber) throws NotFoundException, BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> allDiyEvents = itinerary.getDiy_event_list();
        List<DIYEvent> diyEventsToReturn = new ArrayList<>();

        LocalDate actualDay = itinerary.getStart_date().toLocalDate().plusDays(dayNumber - 1);

        for (DIYEvent diyEvent : allDiyEvents) {
            LocalDate eventStartDate = diyEvent.getStart_datetime().toLocalDate();
            LocalDate eventEndDate = diyEvent.getEnd_datetime().toLocalDate();

            // Check if the event falls within the range of the actual day
            if ((eventStartDate.isEqual(actualDay) || eventStartDate.isBefore(actualDay)) &&
                    (eventEndDate.isEqual(actualDay) || eventEndDate.isAfter(actualDay))) {
                diyEventsToReturn.add(diyEvent);
            }

            if (diyEvent.getBooking() != null) {
                diyEvent.getBooking().setPayment(null);
                diyEvent.getBooking().setLocal_user(null);
                diyEvent.getBooking().setTourist_user(null);
            }
        }

        return diyEventsToReturn;
    }

    // General method for create to make it recyclable
    // Type refers to accommodation, telecom etc., pass in "none" if it's not tied to anything
    // Type id refers to attractionId, telecomId etc., pass in 0 if it's not tied to anything
    public DIYEvent createDiyEvent(Long itineraryId, Long typeId, String type, DIYEvent diyEventToCreate) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        Boolean hasBooking = false;
        if (!type.equalsIgnoreCase("none") && !typeId.equals(0L)) {
            if (type.equalsIgnoreCase("attraction")) {
                Optional<Attraction> attractionOptional = attractionRepository.findById(typeId);
                if (attractionOptional.isEmpty()) {
                    throw new BadRequestException("Attraction does not exist!");
                }

                diyEventToCreate.setAttraction(attractionOptional.get());
                DIYEvent event = diyEventRepository.save(diyEventToCreate);
                itinerary.getDiy_event_list().add(event);
                itineraryRepository.save(itinerary);
                return event;

            } else if (type.equalsIgnoreCase("accommodation")) {
                Optional<Accommodation> accommodationOptional = accommodationRepository.findById(typeId);
                if (accommodationOptional.isEmpty()) {
                    throw new BadRequestException("Accommodation does not exist!");
                }

                Accommodation accommodation = accommodationOptional.get();
                LocalDate checkInDate = diyEventToCreate.getStart_datetime().toLocalDate();
                LocalDate checkOutDate = diyEventToCreate.getEnd_datetime().toLocalDate();
                LocalTime checkInTime = accommodation.getCheck_in_time().toLocalTime();
                LocalTime checkOutTime = accommodation.getCheck_out_time().toLocalTime();

                DIYEvent diyEvent = new DIYEvent();
                diyEvent.setName(diyEventToCreate.getName());
                diyEvent.setLocation(diyEventToCreate.getLocation());
                diyEvent.setRemarks(diyEventToCreate.getRemarks());
                diyEvent.setAccommodation(accommodation);
                diyEvent.setStart_datetime(LocalDateTime.of(checkInDate, checkInTime));
                diyEvent.setEnd_datetime(LocalDateTime.of(checkOutDate, checkOutTime));
                diyEventRepository.save(diyEvent);

//                for (LocalDate date = checkInDate; date.isBefore(checkOutDate.plusDays(1)); date = date.plusDays(1)) {
//                    DIYEvent newDiyEvent = new DIYEvent();
//                    newDiyEvent.setName(diyEventToCreate.getName());
//                    newDiyEvent.setLocation(diyEventToCreate.getLocation());
//                    newDiyEvent.setRemarks(diyEventToCreate.getRemarks());
//                    newDiyEvent.setAccommodation(accommodation);
//
//                    if (date.isEqual(checkInDate)) {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, checkInTime));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                    } else if (date.isEqual(checkOutDate)) {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, checkOutTime));
//                    } else {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                    }
//
//                    diyEvent = diyEventRepository.save(newDiyEvent);
//                    itinerary.getDiy_event_list().add(diyEvent);
//                }

                itinerary.getDiy_event_list().add(diyEvent);
                itineraryRepository.save(itinerary);
                return diyEvent;

            } else if (type.equalsIgnoreCase("telecom")) {
                Optional<Telecom> telecomOptional = telecomRepository.findById(typeId);
                if (telecomOptional.isEmpty()) {
                    throw new BadRequestException("Telecom does not exist!");
                }

                Telecom telecom = telecomOptional.get();
                LocalDate startDate = diyEventToCreate.getStart_datetime().toLocalDate();
                LocalDate endDate = startDate.plusDays(telecom.getNum_of_days_valid());

                DIYEvent diyEvent = new DIYEvent();
                diyEvent.setName(diyEventToCreate.getName());
                diyEvent.setLocation(diyEventToCreate.getLocation());
                diyEvent.setRemarks(diyEventToCreate.getRemarks());
                diyEvent.setTelecom(telecom);
                diyEvent.setStart_datetime(LocalDateTime.of(startDate, LocalTime.of(0, 0)));
                diyEvent.setEnd_datetime(LocalDateTime.of(endDate, LocalTime.of(23, 59)));
                diyEventRepository.save(diyEvent);

//                for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
//                    DIYEvent newDiyEvent = new DIYEvent();
//                    newDiyEvent.setName(diyEventToCreate.getName());
//                    newDiyEvent.setLocation(diyEventToCreate.getLocation());
//                    newDiyEvent.setRemarks(diyEventToCreate.getRemarks());
//                    newDiyEvent.setTelecom(telecom);
//
//                    if (date.isEqual(startDate)) {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                    } else if (date.isEqual(endDate)) {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                    } else {
//                        newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                        newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                    }
//
//                    diyEvent = diyEventRepository.save(newDiyEvent);
//                    itinerary.getDiy_event_list().add(diyEvent);
//                }

                itinerary.getDiy_event_list().add(diyEvent);
                itineraryRepository.save(itinerary);
                return diyEvent;

            } else if (type.equalsIgnoreCase("restaurant")) {
                Optional<Restaurant> restaurantOptional = restaurantRepository.findById(typeId);
                if (restaurantOptional.isEmpty()) {
                    throw new BadRequestException("Restaurant does not exist!");
                }

                diyEventToCreate.setRestaurant(restaurantOptional.get());
                DIYEvent event = diyEventRepository.save(diyEventToCreate);
                itinerary.getDiy_event_list().add(event);
                itineraryRepository.save(itinerary);
                return event;

            } else if (type.equalsIgnoreCase("booking")) {
                Optional<Booking> bookingOptional = bookingRepository.findById(typeId);
                if (bookingOptional.isEmpty()) {
                    throw new BadRequestException("Booking does not exist!");
                }

                diyEventToCreate.setBooking(bookingOptional.get());
                hasBooking = true;
            } else {
                throw new BadRequestException("Invalid type!");
            }
        }

        if (!hasBooking) {
            LocalDate startDate = diyEventToCreate.getStart_datetime().toLocalDate();
            LocalTime startTime = diyEventToCreate.getStart_datetime().toLocalTime();
            LocalDate endDate = diyEventToCreate.getEnd_datetime().toLocalDate();
            LocalTime endTime = diyEventToCreate.getEnd_datetime().toLocalTime();

            DIYEvent diyEvent = new DIYEvent();
            diyEvent.setName(diyEventToCreate.getName());
            diyEvent.setLocation(diyEventToCreate.getLocation());
            diyEvent.setRemarks(diyEventToCreate.getRemarks());
            diyEvent.setStart_datetime(LocalDateTime.of(startDate, startTime));
            diyEvent.setEnd_datetime(LocalDateTime.of(endDate, endTime));
            diyEventRepository.save(diyEvent);

//            for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
//                DIYEvent newDiyEvent = new DIYEvent();
//                newDiyEvent.setName(diyEventToCreate.getName());
//                newDiyEvent.setLocation(diyEventToCreate.getLocation());
//                newDiyEvent.setRemarks(diyEventToCreate.getRemarks());
//
//                if (date.isEqual(startDate)) {
//                    newDiyEvent.setStart_datetime(LocalDateTime.of(date, startTime));
//                    newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                } else if (date.isEqual(endDate)) {
//                    newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                    newDiyEvent.setEnd_datetime(LocalDateTime.of(date, endTime));
//                } else {
//                    newDiyEvent.setStart_datetime(LocalDateTime.of(date, LocalTime.of(0, 0)));
//                    newDiyEvent.setEnd_datetime(LocalDateTime.of(date, LocalTime.of(23, 59)));
//                }
//
//                DIYEvent event = diyEventRepository.save(newDiyEvent);
//                itinerary.getDiy_event_list().add(event);
//                itineraryRepository.save(itinerary);
//            }

                itinerary.getDiy_event_list().add(diyEvent);
                itineraryRepository.save(itinerary);

            return itinerary.getDiy_event_list().get(itinerary.getDiy_event_list().size() - 1); // return last one
        } else {
            DIYEvent diyEvent = diyEventRepository.save(diyEventToCreate);
            itinerary.getDiy_event_list().add(diyEvent);
            itineraryRepository.save(itinerary);
            if (diyEvent.getBooking().getLocal_user() != null) {
                Local local = diyEvent.getBooking().getLocal_user();
                local.setPost_list(null);
                local.setComment_list(null);
                local.setCart_list(null);
                local.setBooking_list(null);
                local.setTour_type_list(null);
                local.setSupport_ticket_list(null);
            } else if (diyEvent.getBooking().getTourist_user() != null) {
                Tourist tourist = diyEvent.getBooking().getTourist_user();
                tourist.setPost_list(null);
                tourist.setComment_list(null);
                tourist.setCart_list(null);
                tourist.setBooking_list(null);
                tourist.setTour_type_list(null);
                tourist.setSupport_ticket_list(null);
            }
            return diyEvent;
        }
    }

    public DIYEvent updateDiyEvent(DIYEvent diyEventToUpdate) throws BadRequestException {
        Optional<DIYEvent> diyEventOptional = diyEventRepository.findById(diyEventToUpdate.getDiy_event_id());
        if (diyEventOptional.isEmpty()) {
            throw new BadRequestException("Event does not exist!");
        }
        DIYEvent diyEvent = diyEventOptional.get();

        diyEvent.setName(diyEventToUpdate.getName());
        diyEvent.setStart_datetime(diyEventToUpdate.getStart_datetime());
        diyEvent.setEnd_datetime(diyEventToUpdate.getEnd_datetime());
        diyEvent.setLocation(diyEventToUpdate.getLocation());
        diyEvent.setRemarks(diyEventToUpdate.getRemarks());

        diyEvent = diyEventRepository.save(diyEvent);
        return diyEvent;
    }

    public String deleteDiyEvent(Long diyEventIdToDelete) throws BadRequestException {
        Optional<DIYEvent> diyEventOptional = diyEventRepository.findById(diyEventIdToDelete);
        if (diyEventOptional.isEmpty()) {
            throw new BadRequestException("Event does not exist!");
        }
        DIYEvent diyEvent = diyEventOptional.get();

        Itinerary itinerary = itineraryRepository.getItineraryContainingDiyEvent(diyEvent.getDiy_event_id());
        itinerary.getDiy_event_list().remove(diyEvent);
        itineraryRepository.save(itinerary);

        diyEventRepository.delete(diyEvent);
        return "Event successfully deleted";
    }

    // minor overlap
    public String diyEventOverlap(Long itineraryId) throws NotFoundException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new NotFoundException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();
        List<DIYEvent> diyEventList = itinerary.getDiy_event_list();

        for (DIYEvent d : diyEventList) {
            for (DIYEvent e : diyEventList) {
                if ((d.getAttraction() != null || d.getRestaurant() != null) && (e.getAttraction() != null || e.getRestaurant() != null)
                        && (long) d.getDiy_event_id() != e.getDiy_event_id() && !notOverlap(d, e)) { // attraction or restaurant overlap
                    return d.getName() + " overlaps with " + e.getName() + "!";

                } else if (d.getAccommodation() != null && e.getAccommodation() != null
                        && (long) d.getDiy_event_id() != e.getDiy_event_id() && !notOverlap(d, e)) { // accommodation overlap
                    return d.getName() + " overlaps with " + e.getName() + "!";

                } else if (d.getTelecom() != null && e.getTelecom() != null
                        && (long) d.getDiy_event_id() != e.getDiy_event_id() && !notOverlap(d, e)) { // telecom overlap
                    return d.getName() + " overlaps with " + e.getName() + "!";

                    // d - diy event, e - attraction or restaurant event
                } else if (d.getBooking() == null && d.getAttraction() == null && d.getTelecom() == null && d.getRestaurant() == null && d.getAccommodation() == null
                        && (e.getAttraction() != null || e.getRestaurant() != null)
                        && (long) d.getDiy_event_id() != e.getDiy_event_id() && !notOverlap(d, e)) { // diy overlap with either attraction or rest
                    return d.getName() + " overlaps with " + e.getName() + "!";
                }

                    // d - diy event, e - diy event
                else if (d.getBooking() == null && d.getAttraction() == null && d.getTelecom() == null && d.getRestaurant() == null && d.getAccommodation() == null
                        && (e.getBooking() == null && e.getAttraction() == null && e.getTelecom() == null && e.getRestaurant() == null && e.getAccommodation() == null)
                        && (long) d.getDiy_event_id() != e.getDiy_event_id() && !notOverlap(d, e)) { // diy overlap with either attraction or rest
                    return d.getName() + " overlaps with " + e.getName() + "!";
                }
            }
        }

        return null;
    }

    // major overlap
    public String diyEventBookingOverlap(Long itineraryId) throws NotFoundException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new NotFoundException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();
        List<DIYEvent> diyEventList = itinerary.getDiy_event_list();

        Set<List<LocalDateTime>> set = new HashSet<>();
        for (DIYEvent d : diyEventList) { // non-booking diy event
            for (DIYEvent e : diyEventList) { // booking diy event
                if (d.getBooking() == null && e.getBooking() != null && (long) d.getDiy_event_id() != e.getDiy_event_id() &&
                        d.getAccommodation() != null && e.getBooking().getRoom() != null && !notOverlap(d, e)) { // accommodation overlap
                    return d.getName() + " overlaps with " + e.getName() + "!";

                } else if (d.getBooking() == null && e.getBooking() != null && (long) d.getDiy_event_id() != e.getDiy_event_id() &&
                        d.getTelecom() != null && e.getBooking().getTelecom() != null && !notOverlap(d, e)) { // telecom overlap
                    return d.getName() + " overlaps with " + e.getName() + "!";

                    // any overlap other than accommodation and telecom
                } else if (d.getBooking() == null && e.getBooking() != null && (long) d.getDiy_event_id() != e.getDiy_event_id() &&
                        d.getAccommodation() == null && d.getTelecom() == null &&
                        e.getBooking().getRoom() == null && e.getBooking().getTelecom() == null && !notOverlap(d, e)) {
                    return d.getName() + " overlaps with " + e.getName() + "!";
                }
            }
        }

        return null;
    }

    private boolean notOverlap(DIYEvent d1, DIYEvent d2) {
        return (d1.getStart_datetime().isAfter(d2.getEnd_datetime()) || d1.getEnd_datetime().isBefore(d2.getStart_datetime())) && !(d1.getStart_datetime().isEqual(d2.getStart_datetime()) && d1.getEnd_datetime().isEqual(d2.getEnd_datetime()));
    }
}
