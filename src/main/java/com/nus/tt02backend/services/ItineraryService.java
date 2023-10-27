package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.InternalRoleEnum;
import com.nus.tt02backend.models.enums.NumberOfValidDaysEnum;
import com.nus.tt02backend.models.enums.SupportTicketTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
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
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            itineraryToReturn = local.getItinerary();
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
        for (DIYEvent diyEvent : diyEventList) {
            diyEventRepository.delete(diyEvent);
        }

        itineraryRepository.delete(itinerary);
    }

    public List<Telecom> getTelecomRecommendations(Long itineraryId) throws BadRequestException {
        Optional<Itinerary> itineraryOptional = itineraryRepository.findById(itineraryId);
        if (itineraryOptional.isEmpty()) {
            throw new BadRequestException("Itinerary does not exist!");
        }
        Itinerary itinerary = itineraryOptional.get();

        List<Telecom> telecomRecommendations = new ArrayList<>();
        Integer numberOfDays = Math.round(Duration.between(itinerary.getStart_date(), itinerary.getEnd_date()).toDays());
        List<Telecom> telecomListForOneDay = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.ONE_DAY));
        List<Telecom> telecomListForThreeDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.THREE_DAY));
        List<Telecom> telecomListForSevenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.SEVEN_DAY));
        List<Telecom> telecomListForFourteenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.FOURTEEN_DAY));
        List<Telecom> telecomListForOverFourteenDays = new ArrayList<>(telecomRepository.getTelecomBasedOnDays(NumberOfValidDaysEnum.MORE_THAN_FOURTEEN_DAYS));

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
        }

        return telecomRecommendations;
    }

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

            return removeDuplicates(attractionRecommendations);
        } else {
            attractionRecommendations.addAll(attractionRepository.findAll());
        }

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
}