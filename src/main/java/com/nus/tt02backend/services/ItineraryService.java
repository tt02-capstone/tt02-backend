package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.ItineraryFriendResponse;
import com.nus.tt02backend.dto.SuggestedEventsResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import jakarta.mail.MessagingException;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItineraryService {
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
    TelecomRepository telecomRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    AccommodationRepository accommodationRepository;
    @Autowired
    AccommodationService accommodationService;
    @Autowired
    RestaurantRepository restaurantRepository;

    public Itinerary getItineraryByUser(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        Itinerary itineraryToReturn = null;

        List<Itinerary> allItineraries = itineraryRepository.findAll();

        for (Itinerary itinerary: allItineraries) {
            if (itinerary.getMaster_id().equals(userId)) {
                itineraryToReturn = itinerary;
                break;
            } else {
                List<Long> addedUsers = itinerary.getAccepted_people_list();
                if (addedUsers.contains(userId)) {
                    itineraryToReturn = itinerary;
                    break;
                }
            }
        }

        if (itineraryToReturn != null ) {
            for (DIYEvent d : itineraryToReturn.getDiy_event_list()) {
                if (d.getBooking() != null) {
                    d.getBooking().setPayment(null);
                    d.getBooking().setTourist_user(null);
                    d.getBooking().setLocal_user(null);
                }
            }
        }

        return itineraryToReturn;
    }

    public Itinerary getItineraryByUserForOtherFunc(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

       if (user instanceof Tourist) {
           Tourist t = (Tourist) user;
           return t.getItinerary();
       } else if (user instanceof Local) {
           Local l = (Local) user;
           return l.getItinerary();
       } else {
           throw new BadRequestException("User is not a tourist or local!");
       }
    }

    public Itinerary createItinerary(Long userId, Itinerary itineraryToCreate) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        itineraryToCreate.setDiy_event_list(new ArrayList<>());

        itineraryToCreate.setAccepted_people_list(new ArrayList<>());
        itineraryToCreate.setInvited_people_list(new ArrayList<>());
        itineraryToCreate.setMaster_id(userId);
        Itinerary itinerary = itineraryRepository.save(itineraryToCreate);

        if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            tourist.setItinerary(itineraryToCreate);
            touristRepository.save(tourist);
            System.out.println("here4" + tourist.getItinerary().getItinerary_id());

        } else if (user instanceof Local) {
            Local local = (Local) user;
            local.setItinerary(itineraryToCreate);
            localRepository.save(local);
        }

        // add diy events for bookings made before itinerary was created
        if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            List<DIYEvent> diyEventList = diyEventRepository.getDiyEventByTouristIdAndDate(tourist.getUser_id(), itineraryToCreate.getStart_date(), itineraryToCreate.getEnd_date());
            itinerary.setDiy_event_list(diyEventList);
            itineraryRepository.save(itinerary);
        } else {
            Local local = (Local) user;
            List<DIYEvent> diyEventList = diyEventRepository.getDiyEventByLocalIdAndDate(local.getUser_id(), itineraryToCreate.getStart_date(), itineraryToCreate.getEnd_date());
            itinerary.setDiy_event_list(diyEventList);
            itineraryRepository.save(itinerary);
        }

        return itinerary;
    }

    public Itinerary updateItinerary(Long itineraryId, Itinerary itineraryToUpdate) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        itinerary.setStart_date(itineraryToUpdate.getStart_date());
        itinerary.setEnd_date(itineraryToUpdate.getEnd_date());
        itinerary.setNumber_of_pax(itineraryToUpdate.getNumber_of_pax());
        itinerary.setRemarks(itineraryToUpdate.getRemarks());

        itinerary = itineraryRepository.save(itinerary);

        return itinerary;
    }

    public void deleteItinerary(Long userId, Long itineraryId) throws NotFoundException, BadRequestException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.setItinerary(null);
            touristRepository.save(tourist);
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.setItinerary(null);
            localRepository.save(local);
        }

        List<DIYEvent> diyEventList = itinerary.getDiy_event_list();
        itinerary.setDiy_event_list(null);
        for (DIYEvent diyEvent : diyEventList) {
            if (diyEvent.getBooking() == null) {
                System.out.println("deleted: " + diyEvent.getDiy_event_id());
                diyEventRepository.delete(diyEvent);
            }
        }

        itineraryRepository.delete(itinerary);
    }

    // Telecom Recommendations
    /*
        - Logic
        1) Find telecom packages where itinerary duration <= number of days valid
        2) If none, find the next tier of number of days valid (e.g. itinerary is 1 day, if no one_day, find three_day)
        3) If none, return any 3 telecom
     */
    public List<Telecom> getTelecomRecommendations(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<Telecom> telecomRecommendations = new ArrayList<>();
        Integer numberOfDays = Math.round(Duration.between(itinerary.getStart_date(), itinerary.getEnd_date()).toDays());
        List<Telecom> telecomListForOneDay = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.ONE_DAY).subList(0, Math.min(3, telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.ONE_DAY).size())));
        List<Telecom> telecomListForThreeDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.THREE_DAY).subList(0, Math.min(3, telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.THREE_DAY).size())));
        List<Telecom> telecomListForSevenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.SEVEN_DAY).subList(0, Math.min(3, telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.SEVEN_DAY).size())));
        List<Telecom> telecomListForFourteenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.FOURTEEN_DAY).subList(0, Math.min(3, telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.FOURTEEN_DAY).size())));
        List<Telecom> telecomListForOverFourteenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.MORE_THAN_FOURTEEN_DAYS).subList(0, Math.min(3, telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.MORE_THAN_FOURTEEN_DAYS).size())));

        if (numberOfDays <= 1) {
            if (!telecomListForOneDay.isEmpty()) {
                telecomRecommendations.addAll(telecomListForOneDay);
            } else if (!telecomListForThreeDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForThreeDays);
            } else if (!telecomListForSevenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForSevenDays);
            } else if (!telecomListForFourteenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForFourteenDays);
            } else {
                telecomRecommendations.addAll(telecomListForOverFourteenDays);
            }
        } else if (numberOfDays <= 3) {
            if (!telecomListForThreeDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForThreeDays);
            } else if (!telecomListForSevenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForSevenDays);
            } else if (!telecomListForFourteenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForFourteenDays);
            } else {
                telecomRecommendations.addAll(telecomListForOverFourteenDays);
            }
        } else if (numberOfDays <= 7) {
            if (!telecomListForSevenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForSevenDays);
            } else if (!telecomListForFourteenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForFourteenDays);
            } else {
                telecomRecommendations.addAll(telecomListForOverFourteenDays);
            }
        } else if (numberOfDays <= 14) {
            if (!telecomListForFourteenDays.isEmpty()) {
                telecomRecommendations.addAll(telecomListForFourteenDays);
            } else {
                telecomRecommendations.addAll(telecomListForOverFourteenDays);
            }
        } else {
            telecomRecommendations.addAll(telecomListForOverFourteenDays);

            // Return any 3 telecom
            if (!telecomRecommendations.isEmpty()) {
                List<Telecom> allTelecoms = telecomRepository.findAll();
                telecomRecommendations.addAll(allTelecoms.subList(0, Math.min(3, allTelecoms.size())));
            }
        }

        return telecomRecommendations;
    }

    // Attraction Recommendations
    /*
        - Logic
        1) Given a particular date, search for empty slots between 2 events and find attractions with suggested_duration <= empty slot duration
        2) If none, return any 3 attractions
     */
    public List<Attraction> getAttractionRecommendationsByDate(Long itineraryId, LocalDate dateTime) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> events = itinerary.getDiy_event_list();
        List<DIYEvent> eventsOnCurrentDate = getEventsOnDate(events, dateTime);
        List<Attraction> attractionRecommendations = new ArrayList<>();
        if (!events.isEmpty() && !eventsOnCurrentDate.isEmpty()) {
            processEventsForAttractions(eventsOnCurrentDate, attractionRecommendations);
            if (!attractionRecommendations.isEmpty()) {
                return removeDuplicates(attractionRecommendations);
            }
        }

        // Return any 3 attractions
        List<Attraction> allAttractions = attractionRepository.findAll();
        attractionRecommendations.addAll(allAttractions.subList(0, Math.min(3, allAttractions.size())));

        return attractionRecommendations;
    }

    public List<Attraction> getAttractionRecommendationsForItinerary(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> events = itinerary.getDiy_event_list();
        List<Attraction> attractionRecommendations = new ArrayList<>();
        if (!events.isEmpty()) {
            Integer numberOfDays = Math.round(Duration.between(itinerary.getStart_date(), itinerary.getEnd_date()).toDays());
            LocalDate currentDate = events.get(0).getStart_datetime().toLocalDate();

            for (int i = 0; i < numberOfDays; i++) {
                List<DIYEvent> eventsOnCurrentDate = getEventsOnDate(events, currentDate);
                processEventsForAttractions(eventsOnCurrentDate, attractionRecommendations);
                currentDate = currentDate.plusDays(1);
            }

            return removeDuplicates(attractionRecommendations);
        } else {
            attractionRecommendations.addAll(attractionRepository.findAll());
        }

        return attractionRecommendations;
    }

    private void processEventsForAttractions(List<DIYEvent> events, List<Attraction> attractionRecommendations) {

        // Remove accommodation and telecom from list as it's a full-day thing
        List<DIYEvent> filteredEvents = new ArrayList<>();
        for (DIYEvent event : events) {
            if (event.getAccommodation() == null && event.getTelecom() == null) {
                filteredEvents.add(event);
            }
        }
        events = filteredEvents;

        for (int j = 0; j < events.size() - 1; j++) {
            DIYEvent currentEvent = events.get(j);
            DIYEvent nextEvent = events.get(j + 1);
            Integer durationBetweenEvents = calculateDurationBetweenEvents(currentEvent, nextEvent);

            // If negative value, indicates overlap
            if (durationBetweenEvents >= 0) {
                List<Attraction> attractionsThatFitIntoDuration = getAttractionsByDuration(durationBetweenEvents);
                attractionRecommendations.addAll(attractionsThatFitIntoDuration);
            }
        }
    }

    private List<Attraction> getAttractionsByDuration(int duration) {
        return attractionRepository.getAttractionsByDuration(duration);
    }

    private Integer calculateDurationBetweenEvents(DIYEvent currentEvent, DIYEvent nextEvent) {
        return Math.round(Duration.between(currentEvent.getEnd_datetime(), nextEvent.getStart_datetime()).toHours());
    }

    private List<DIYEvent> getEventsOnDate(List<DIYEvent> events, LocalDate currentDate) {
        return events.stream()
                .filter(event -> event.getStart_datetime().toLocalDate().isEqual(currentDate))
                .toList();
    }

    private <T> List<T> removeDuplicates(List<T> items) {
        Set<T> uniqueItems = new HashSet<>(items);
        return new ArrayList<>(uniqueItems);
    }

    // Accommodation Recommendations
    /*
        - Logic
        1) Recommend accommodations that are near/at the same location as DIYEvent and available for booking
        2) If no common location, recommend accommodations that are available for booking
        3) If none, return any 3 accommodation
     */
    public List<Accommodation> getAccommodationRecommendationsForItinerary(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        // Format the generic locations
        Map<String, Integer> genericLocationsCount = new HashMap<>();
        Map<String, GenericLocationEnum> originalGenericLocations = new HashMap<>();
        for (GenericLocationEnum location : GenericLocationEnum.values()) {
            String formattedLocation = location.toString().toLowerCase().replace("_", " ");
            genericLocationsCount.put(formattedLocation, 0);

            originalGenericLocations.put(formattedLocation, location);
        }

        List<DIYEvent> events = itinerary.getDiy_event_list();
        List<Accommodation> accommodations = accommodationRepository.findAll();
        List<Accommodation> accommodationRecommendations = new ArrayList<>();

        // Based on user's events list, find the locations that are most common (so can recommend accoms near there)
        for (String key : genericLocationsCount.keySet()) {
            List<DIYEvent> filteredDiyEvents = events.stream()
                    .filter(event -> event.getLocation().toLowerCase().contains(key))
                    .toList();

            genericLocationsCount.put(key, filteredDiyEvents.size());
        }

        Map<String, Integer> sortedGenericLocationsCount = genericLocationsCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<Accommodation> filteredAccommodations = new ArrayList<>();
        // Check accoms availability for those locations where the count isn't 0
        checkAccommodationsForLocation(accommodationRecommendations, filteredAccommodations, sortedGenericLocationsCount, originalGenericLocations, itinerary);

        if (!accommodationRecommendations.isEmpty()) {
            return removeDuplicates(accommodationRecommendations);
        } else {
            // Accoms with common location are not available, just find any 3 accoms that are available for booking
            List<RoomTypeEnum> roomTypes = List.of(RoomTypeEnum.values());
            for (Accommodation accommodation : accommodations) {
                for (RoomTypeEnum roomType : roomTypes) {
                    try {
                        if (accommodationService.isRoomAvailableOnDateRange(accommodation.getAccommodation_id(), roomType, itinerary.getStart_date(), itinerary.getEnd_date())) {
                            accommodationRecommendations.add(accommodation);
                            break;
                        }
                    } catch (NotFoundException | BadRequestException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (!accommodationRecommendations.isEmpty()) {
                return removeDuplicates(accommodationRecommendations.subList(0, Math.min(3, accommodationRecommendations.size())));
            } else {
                // All accoms are unavailable, just return any 3 accoms
                return accommodations.subList(0, Math.min(3, accommodations.size()));
            }
        }
    }

    private void checkAccommodationsForLocation(List<Accommodation> accommodationRecommendations, List<Accommodation> filteredAccommodations, Map<String, Integer> sortedGenericLocationsCount, Map<String, GenericLocationEnum> originalGenericLocations, Itinerary itinerary) {
        sortedGenericLocationsCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .forEach(entry -> {
                    GenericLocationEnum currentLocation = originalGenericLocations.get(entry.getKey());
                    filteredAccommodations.addAll(accommodationRepository.getAccommodationByGenericLocation(currentLocation));

                    List<RoomTypeEnum> roomTypes = List.of(RoomTypeEnum.values());
                    for (Accommodation accommodation : filteredAccommodations) {
                        for (RoomTypeEnum roomType : roomTypes) {
                            try {
                                if (accommodationService.isRoomAvailableOnDateRange(accommodation.getAccommodation_id(), roomType, itinerary.getStart_date(), itinerary.getEnd_date())) {
                                    accommodationRecommendations.add(accommodation);
                                    break;
                                }
                            } catch (NotFoundException | BadRequestException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    filteredAccommodations.clear();
                });
    }

    // Restaurant Recommendations
    /*
        - Logic
        1) Recommend restaurants that are near/at the same location as DIYEvent
        2) If no common location, return any 3 restaurant
     */
    public List<Restaurant> getRestaurantRecommendationsForItinerary(Long itineraryId, LocalDate dateTime) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> events = itinerary.getDiy_event_list();
        List<DIYEvent> eventsOnCurrentDate = getEventsOnDate(events, dateTime);
        List<Restaurant> restaurantRecommendations = new ArrayList<>();
        if (!events.isEmpty() && !eventsOnCurrentDate.isEmpty()) {
            // Format the generic locations
            Map<String, Integer> genericLocationsCount = new HashMap<>();
            Map<String, GenericLocationEnum> originalGenericLocations = new HashMap<>();
            for (GenericLocationEnum location : GenericLocationEnum.values()) {
                String formattedLocation = location.toString().toLowerCase().replace("_", " ");
                genericLocationsCount.put(formattedLocation, 0);

                originalGenericLocations.put(formattedLocation, location);
            }

            // Based on user's events list, find the locations that are most common (so can recommend restaurants near there)
            for (String key : genericLocationsCount.keySet()) {
                List<DIYEvent> filteredDiyEvents = events.stream()
                        .filter(event -> event.getLocation().toLowerCase().contains(key))
                        .toList();

                genericLocationsCount.put(key, filteredDiyEvents.size());
            }

            Map<String, Integer> sortedGenericLocationsCount = genericLocationsCount.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

            sortedGenericLocationsCount.entrySet().stream()
                    .filter(entry -> entry.getValue() > 0)
                    .forEach(entry -> {
                        GenericLocationEnum currentLocation = originalGenericLocations.get(entry.getKey());
                        restaurantRecommendations.addAll(restaurantRepository.getRestaurantByGenericLocation(currentLocation));
                    });

            if (!restaurantRecommendations.isEmpty()) {
                return removeDuplicates(restaurantRecommendations);
            }
        }

        // Return any 3 restaurants
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        restaurantRecommendations.addAll(allRestaurants.subList(0, Math.min(3, allRestaurants.size())));

        return restaurantRecommendations;
    }

    public SuggestedEventsResponse getSuggestedEventsBasedOnTimeslot(LocalTime startTime, LocalTime endTime) throws BadRequestException {
        Integer durationInHours = (int) Duration.between(startTime, endTime).toHours();

        SuggestedEventsResponse suggestedEvents = new SuggestedEventsResponse();
        suggestedEvents.setRestaurants(new ArrayList<>());
        suggestedEvents.setAttractions(new ArrayList<>());

        suggestedEvents.getRestaurants().addAll(restaurantRepository.getRestaurantsByDuration(durationInHours));
        suggestedEvents.getAttractions().addAll(attractionRepository.getAttractionsByDuration(durationInHours));

        if (suggestedEvents.getRestaurants().isEmpty() && suggestedEvents.getAttractions().isEmpty()) {
            throw new BadRequestException("There are no events available between the specified start and end times");
        } else {
            if (!suggestedEvents.getAttractions().isEmpty()) {
                List<Attraction> filteredAttractions = new ArrayList<>();

                for (Attraction attraction : suggestedEvents.getAttractions()) {
                    String[] times = attraction.getOpening_hours().split(" - ");

                    LocalTime openTime;
                    LocalTime closeTime;
                    DateTimeFormatter formatterWithDot = DateTimeFormatter.ofPattern("h.mma");
                    DateTimeFormatter formatterWithoutDot = DateTimeFormatter.ofPattern("ha");

                    if (times[0].trim().contains(".")) {
                        openTime = LocalTime.parse(times[0].trim(), formatterWithDot);
                    } else {
                        openTime = LocalTime.parse(times[0].trim(), formatterWithoutDot);
                    }

                    if (times[1].trim().contains(".")) {
                        closeTime = LocalTime.parse(times[1].trim(), formatterWithDot);
                    } else {
                        closeTime = LocalTime.parse(times[1].trim(), formatterWithoutDot);
                    }

                    if ((startTime.isAfter(openTime) || startTime.equals(openTime))
                            && (endTime.isBefore(closeTime) || endTime.equals(closeTime))) {
                        filteredAttractions.add(attraction);
                    }
                }

                if (filteredAttractions.size() != suggestedEvents.getAttractions().size()) {
                    suggestedEvents.getAttractions().clear();
                    suggestedEvents.getAttractions().addAll(filteredAttractions);
                }
            }

            if (!suggestedEvents.getRestaurants().isEmpty()) {
                List<Restaurant> filteredRestaurants = new ArrayList<>();

                for (Restaurant restaurant : suggestedEvents.getRestaurants()) {
                    String[] times = restaurant.getOpening_hours().split(" - ");

                    LocalTime openTime;
                    LocalTime closeTime;
                    DateTimeFormatter formatterWithDot = DateTimeFormatter.ofPattern("h.mma");
                    DateTimeFormatter formatterWithoutDot = DateTimeFormatter.ofPattern("ha");

                    if (times[0].trim().contains(".")) {
                        openTime = LocalTime.parse(times[0].trim(), formatterWithDot);
                    } else {
                        openTime = LocalTime.parse(times[0].trim(), formatterWithoutDot);
                    }

                    if (times[1].trim().contains(".")) {
                        closeTime = LocalTime.parse(times[1].trim(), formatterWithDot);
                    } else {
                        closeTime = LocalTime.parse(times[1].trim(), formatterWithoutDot);
                    }

                    if ((startTime.isAfter(openTime) || startTime.equals(openTime))
                            && (endTime.isBefore(closeTime) || endTime.equals(closeTime))) {
                        filteredRestaurants.add(restaurant);
                    }
                }

                if (filteredRestaurants.size() != suggestedEvents.getRestaurants().size()) {
                    suggestedEvents.getRestaurants().clear();
                    suggestedEvents.getRestaurants().addAll(filteredRestaurants);
                }
            }
        }

        if (suggestedEvents.getRestaurants().isEmpty() && suggestedEvents.getAttractions().isEmpty()) {
            throw new BadRequestException("There are no events available between the specified start and end times");
        }

        return suggestedEvents;
    }

    public Boolean existingAccommodationInItinerary(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> events = itinerary.getDiy_event_list();
        if (!events.isEmpty()) {
            for (DIYEvent diyEvent : events) {
                if (diyEvent.getAccommodation() != null) {
                    return true;
                } else if (diyEvent.getBooking() != null && diyEvent.getBooking().getRoom() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public Boolean existingTelecomInItinerary(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<DIYEvent> events = itinerary.getDiy_event_list();
        if (!events.isEmpty()) {
            for (DIYEvent diyEvent : events) {
                if (diyEvent.getTelecom() != null) {
                    return true;
                } else if (diyEvent.getBooking() != null && diyEvent.getBooking().getTelecom() != null) {
                    return true;
                }
            }
        }

        return false;
    }

    public List<ItineraryFriendResponse> getUserWithEmailSimilarity(Long masterUserId, Long itineraryId, String email) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        List<User> userList = new ArrayList<>();
        if (email.equals("undefined")) {
            userList = userRepository.getUserListWithEmailSimilarity(masterUserId, "");
        } else {
            userList = userRepository.getUserListWithEmailSimilarity(masterUserId, email);
        }
        List<ItineraryFriendResponse> list = new ArrayList<>();

        for (User u : userList) {
            if (!itinerary.getAccepted_people_list().contains(u.getUser_id()) && !itinerary.getInvited_people_list().contains(u.getUser_id())) {
                list.add(new ItineraryFriendResponse(u.getUser_id(), u.getEmail(), u.getName(), u.getProfile_pic()));
            }
        }

        return list;
    }

    public List<ItineraryFriendResponse> getInvitedUsers(Long itineraryId) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        List<ItineraryFriendResponse> list = new ArrayList<>();
        for (Long l : itinerary.getInvited_people_list()) {
            Optional<User> userOptional = userRepository.findById(l);
            if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
            User u = userOptional.get();
            list.add(new ItineraryFriendResponse(u.getUser_id(), u.getEmail(), u.getName(), u.getProfile_pic()));
        }

        return list;
    }

    public List<ItineraryFriendResponse> getAcceptedUsers(Long itineraryId) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        List<ItineraryFriendResponse> list = new ArrayList<>();
        for (Long l : itinerary.getAccepted_people_list()) {
            Optional<User> userOptional = userRepository.findById(l);
            if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
            User u = userOptional.get();
            list.add(new ItineraryFriendResponse(u.getUser_id(), u.getEmail(), u.getName(), u.getProfile_pic()));
        }

        return list;
    }

    public String toggleItineraryInvite(Long itineraryId, Long userIdToAddOrRemove) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        if (itinerary.getInvited_people_list().contains(userIdToAddOrRemove)) { // if already added, remove
            itinerary.getInvited_people_list().remove(userIdToAddOrRemove);
            itineraryRepository.save(itinerary);
            return "User removed!";
        } else { // if not yet added, add id in
            itinerary.getInvited_people_list().add((userIdToAddOrRemove));
            itineraryRepository.save(itinerary);
            return "User invited!";
        }
    }

    public List<String> getProfileImageByIdList(Long itineraryId) throws NotFoundException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        List<String> list = new ArrayList<>();
        for (Long i : itinerary.getAccepted_people_list()) {
            String profilePic = userRepository.getProfileImageByIdList(i);
            if (profilePic.length() > 4) list.add(profilePic);
        }

        String masterProfilePic = userRepository.getProfileImageByIdList(itinerary.getMaster_id());
        if (masterProfilePic.length() > 4) list.add(masterProfilePic);

        return list;
    }

    public List<Itinerary> getInvitationsByUser(Long userId) {

        List<Itinerary> allItineraries = itineraryRepository.findAll();
        List<Itinerary> itinerariesInvitedTo = new ArrayList<>();

        for (Itinerary itinerary : allItineraries) {
            List<Long> invitedUsers = itinerary.getInvited_people_list();
            for (Long user : invitedUsers) {
                if (Objects.equals(user, userId)) {
                    itinerary.setDiy_event_list(null);
                    itinerariesInvitedTo.add(itinerary);
                }
            }
        }

        return itinerariesInvitedTo;
    }

    public String addUserToItinerary(Long itineraryId, Long userId) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        itinerary.getInvited_people_list().remove(userId);
        itinerary.getAccepted_people_list().add(userId);
        itineraryRepository.save(itinerary);
        return "User added!";
    }

    public String removeUserFromItinerary(Long itineraryId, Long userId) throws NotFoundException {

        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) throw new NotFoundException("Itinerary not found!");
        Itinerary itinerary = itineraryOptional.get();

        itinerary.getAccepted_people_list().remove(userId);
        itineraryRepository.save(itinerary);
        return "User removed!";
    }

    public ItineraryFriendResponse getItineraryMasterUserEmail(Long userId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        return new ItineraryFriendResponse(user.getUser_id(), user.getEmail(), user.getName(), user.getProfile_pic());
    }
}