package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BadgeTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            if (diyEvent.getStart_datetime().toLocalDate().equals(actualDay)) {
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

        if (!type.equalsIgnoreCase("none") && !typeId.equals(0L)) {
            if (type.equalsIgnoreCase("attraction")) {
                Optional<Attraction> attractionOptional = attractionRepository.findById(typeId);
                if (attractionOptional.isEmpty()) {
                    throw new BadRequestException("Attraction does not exist!");
                }

                diyEventToCreate.setAttraction(attractionOptional.get());
            } else if (type.equalsIgnoreCase("accommodation")) {
                Optional<Accommodation> accommodationOptional = accommodationRepository.findById(typeId);
                if (accommodationOptional.isEmpty()) {
                    throw new BadRequestException("Accommodation does not exist!");
                }

                diyEventToCreate.setAccommodation(accommodationOptional.get());
            } else if (type.equalsIgnoreCase("telecom")) {
                Optional<Telecom> telecomOptional = telecomRepository.findById(typeId);
                if (telecomOptional.isEmpty()) {
                    throw new BadRequestException("Telecom does not exist!");
                }

                diyEventToCreate.setTelecom(telecomOptional.get());
            } else if (type.equalsIgnoreCase("restaurant")) {
                Optional<Restaurant> restaurantOptional = restaurantRepository.findById(typeId);
                if (restaurantOptional.isEmpty()) {
                    throw new BadRequestException("Restaurant does not exist!");
                }

                diyEventToCreate.setRestaurant(restaurantOptional.get());
            } else if (type.equalsIgnoreCase("booking")) {
                Optional<Booking> bookingOptional = bookingRepository.findById(typeId);
                if (bookingOptional.isEmpty()) {
                    throw new BadRequestException("Booking does not exist!");
                }

                diyEventToCreate.setBooking(bookingOptional.get());
            } else {
                throw new BadRequestException("Invalid type!");
            }
        }

        DIYEvent diyEvent = diyEventRepository.save(diyEventToCreate);
        itinerary.getDiy_event_list().add(diyEvent);
        itineraryRepository.save(itinerary);

        return diyEvent;
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
}