package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.RecommendationResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    AttractionService attractionService;

    @Autowired
    AccommodationService accommodationService;

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    TourService tourService;

    @Autowired
    TelecomService telecomService;


    public List<Object> getRecommendation(GenericLocationEnum location, ListingTypeEnum listingType, Long typeId) throws NotFoundException {

        List<Object> combinedList = new ArrayList<>();

        if (listingType == ListingTypeEnum.ATTRACTION) {
            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location, typeId);
            List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(location);
            List<Accommodation> acList = accommodationService.nearbyAccommRecommendation(location);

            if (!aList.isEmpty() || !rList.isEmpty() || !acList.isEmpty()) {
                combinedList.addAll(aList);
                combinedList.addAll(rList);
                combinedList.addAll(acList);

                Collections.shuffle(combinedList, new Random()); // anyhow shuffle so it wont return the same things
                return combinedList.subList(0,3);
            } else {
                return new ArrayList<>(); // return by blank if there is nth ard
            }

        } else if (listingType ==  ListingTypeEnum.RESTAURANT) {
            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location);
            List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(location,typeId);
            List<Accommodation> acList = accommodationService.nearbyAccommRecommendation(location);

            if (!aList.isEmpty() || !rList.isEmpty() || !acList.isEmpty()) {
                combinedList.addAll(aList);
                combinedList.addAll(rList);
                combinedList.addAll(acList);

                Collections.shuffle(combinedList, new Random());
                return combinedList.subList(0,3);
            } else {
                return new ArrayList<>();
            }
        } else if (listingType ==  ListingTypeEnum.ACCOMMODATION) {
            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location);
            List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(location);
            List<Accommodation> acList = accommodationService.nearbyAccommRecommendation(location,typeId);

            if (!aList.isEmpty() || !rList.isEmpty() || !acList.isEmpty()) {
                combinedList.addAll(aList);
                combinedList.addAll(rList);
                combinedList.addAll(acList);

                Collections.shuffle(combinedList, new Random());
                return combinedList.subList(0,3);
            } else {
                return new ArrayList<>();
            }
        } else {
            throw new NotFoundException("No recommendations to return after finding for recommendation");
        }
    }

//    public List<Object> getRecommendation(GenericLocationEnum location, ListingTypeEnum listingType, Long typeId) throws NotFoundException {
//
//        List<Object> combinedList = new ArrayList<>();
//
//        if (listingType == ListingTypeEnum.ATTRACTION) {
//            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location, typeId);
//            List<Accommodation> acList = accommodationService.nearbyAccommRecommendation(location);
//
//            if (!aList.isEmpty() || !acList.isEmpty()) {
//                combinedList.addAll(aList);
//                combinedList.addAll(acList);
//
//                Collections.shuffle(combinedList, new Random()); // anyhow shuffle so it wont return the same things
//                return combinedList.subList(0,2); // increase to 3 in SR3
//            } else {
//                return new ArrayList<>(); // return by blank if there is nth ard
//            }
//
//        } else if (listingType ==  ListingTypeEnum.ACCOMMODATION) {
//            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location);
//            List<Accommodation> acList = accommodationService.nearbyAccommRecommendation(location,typeId);
//
//            if (!aList.isEmpty() || !acList.isEmpty()) {
//                combinedList.addAll(aList);
//                combinedList.addAll(acList);
//
//                Collections.shuffle(combinedList, new Random());
//                return combinedList.subList(0,2);
//            } else {
//                return new ArrayList<>();
//            }
//        } else {
//            throw new NotFoundException("No recommendations to return after finding for recommendation");
//        }
//    }


    public RecommendationResponse getRecommendationFromBookings(Long userId) throws NotFoundException, BadRequestException {
        List<Booking> bookingList = bookingService.getAllBookingsByUser(userId);

        Set<Telecom> telecomList = new HashSet<>();
        Set<Attraction> attractionList = new HashSet<>();
        Set<Accommodation> accommodationList =  new HashSet<>();
        Set<Restaurant> restaurantList = new HashSet<>();
        Set<Tour> tourList =  new HashSet<>();

        for (Booking booking: bookingList) {

            if(booking.getType().equals(BookingTypeEnum.TELECOM)) {
                Telecom telecom = booking.getTelecom();
                List<Telecom>  pList = telecomService.getSimilarTierTelecom(telecom.getEstimated_price_tier());
                List<Telecom>  dList = telecomService.getSimilarDurationTelecom(telecom.getPlan_duration_category());


                if (!pList.isEmpty() || !dList.isEmpty()) {
                    telecomList.addAll(pList);
                    telecomList.addAll(dList);
                }

            } else if (booking.getType().equals(BookingTypeEnum.ACCOMMODATION)) {
                Room room = booking.getRoom();
                Accommodation accommodation = accommodationService.retrieveAccommodationByRoom(room.getRoom_id());
                List<Accommodation> pList = accommodationService.similarPriceAccommRecommendation(accommodation.getEstimated_price_tier(), accommodation.getAccommodation_id());
                List<Accommodation> dList = accommodationService.nearbyAccommRecommendation(accommodation.getGeneric_location(), accommodation.getAccommodation_id());

                if (!pList.isEmpty() || !dList.isEmpty()) {
                    accommodationList.addAll(pList);
                    accommodationList.addAll(dList);
                }

            } else if (booking.getType().equals(BookingTypeEnum.ATTRACTION)) {
                Attraction attraction = booking.getAttraction();
                attraction.getEstimated_price_tier();
                List<Attraction> aList = attractionService.nearbyAttrRecommendation(attraction.getGeneric_location(), attraction.getAttraction_id());
                List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(attraction.getGeneric_location());

                if (!aList.isEmpty() ) {
                    attractionList.addAll(aList);
                }

                if (!rList.isEmpty()) {
                    restaurantList.addAll(rList);

                }
            } else if (booking.getType().equals(BookingTypeEnum.TOUR)) {

            }

        }

        return new RecommendationResponse(
                attractionList.stream().toList(),
                telecomList.stream().toList(),
                accommodationList.stream().toList(),
                restaurantList.stream().toList(),
                tourList.stream().toList()
        );
    }

}

