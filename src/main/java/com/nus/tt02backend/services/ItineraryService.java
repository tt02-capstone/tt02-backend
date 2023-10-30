package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
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

        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            itineraryToReturn = tourist.getItinerary();

            for (DIYEvent d : itineraryToReturn.getDiy_event_list()) {
                if (d.getBooking() != null) {
                    d.getBooking().setPayment(null);
                    d.getBooking().setTourist_user(null);
                    d.getBooking().setLocal_user(null);
                }
            }
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            itineraryToReturn = local.getItinerary();

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

    public Itinerary createItinerary(Long userId, Itinerary itineraryToCreate) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        itineraryToCreate.setDiy_event_list(new ArrayList<>());

        Itinerary itinerary = itineraryRepository.save(itineraryToCreate);

        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.setItinerary(itinerary);
            touristRepository.save(tourist);

        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.setItinerary(itinerary);
            localRepository.save(local);

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
            diyEventRepository.delete(diyEvent);
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
}