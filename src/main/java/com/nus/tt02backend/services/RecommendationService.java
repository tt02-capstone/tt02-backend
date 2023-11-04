package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.RecommendationResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
import com.nus.tt02backend.repositories.AccommodationRepository;
import com.nus.tt02backend.repositories.AttractionRepository;
import com.nus.tt02backend.repositories.CategoryItemRepository;
import com.nus.tt02backend.repositories.RestaurantRepository;
import com.nus.tt02backend.repositories.TourTypeRepository;
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
    TourTypeRepository tourTypeRepository;

    @Autowired
    TourService tourService;

    @Autowired
    TelecomService telecomService;


    @Autowired
    CategoryItemRepository categoryItemRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

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

    public List<Object> getPostRecommendation(Long catId) throws NotFoundException {

        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(catId);

        if (categoryItemOptional.isPresent()) {
            CategoryItem categoryItem = categoryItemOptional.get();
            String name = categoryItem.getName();

            Attraction attraction = attractionRepository.getAttractionByName(name);
            if (attraction != null) {
                return this.getRecommendation(attraction.getGeneric_location(), attraction.getListing_type(), attraction.getAttraction_id());
            }

            Accommodation accommodation = accommodationRepository.getAccommodationByName(name);
            if (accommodation != null) {
                return this.getRecommendation(accommodation.getGeneric_location(), accommodation.getListing_type(), accommodation.getAccommodation_id());
            }

            Restaurant restaurant = restaurantRepository.getRestaurantByName(name);
            if (restaurant != null) {
                return this.getRecommendation(restaurant.getGeneric_location(), restaurant.getListing_type(), restaurant.getRestaurant_id());
            }

            return new ArrayList<>(); // by default, recommended list is empty
        } else {
            throw new NotFoundException("Category item is not found!");
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
        Set<TourType> tourTypeList =  new HashSet<>();

        Set<Telecom> telecomListOld = new HashSet<>();
        Set<Attraction> attractionListOld = new HashSet<>();
        Set<Accommodation> accommodationListOld =  new HashSet<>();
        Set<Restaurant> restaurantListOld = new HashSet<>();
        Set<TourType> tourTypeListOld =  new HashSet<>();

        for (Booking booking: bookingList) {

            if(booking.getType().equals(BookingTypeEnum.TELECOM)) {
                Telecom telecom = booking.getTelecom();
                telecomListOld.add(telecom);
                List<Telecom>  pList = telecomService.getSimilarTierTelecom(telecom.getEstimated_price_tier());
                List<Telecom>  dList = telecomService.getSimilarDurationTelecom(telecom.getPlan_duration_category());

                if (!pList.isEmpty() || !dList.isEmpty()) {
                    telecomList.addAll(pList);
                    telecomList.addAll(dList);
                }

            } else if (booking.getType().equals(BookingTypeEnum.ACCOMMODATION)) {
                Room room = booking.getRoom();
                Accommodation accommodation = accommodationService.retrieveAccommodationByRoom(room.getRoom_id());
                accommodationListOld.add(accommodation);
                List<Accommodation> pList = accommodationService.similarPriceAccommRecommendation(accommodation.getEstimated_price_tier(), accommodation.getAccommodation_id());
                List<Accommodation> dList = accommodationService.nearbyAccommRecommendation(accommodation.getGeneric_location(), accommodation.getAccommodation_id());

                if (!pList.isEmpty() || !dList.isEmpty()) {
                    accommodationList.addAll(pList);
                    accommodationList.addAll(dList);
                }

            } else if (booking.getType().equals(BookingTypeEnum.ATTRACTION)) {
                Attraction attraction = booking.getAttraction();
                attractionListOld.add(attraction);
//                attraction.getEstimated_price_tier();
                List<Attraction> aList = attractionService.nearbyAttrRecommendation(attraction.getGeneric_location(), attraction.getAttraction_id());
                List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(attraction.getGeneric_location());

                if (!aList.isEmpty() ) {
                    attractionList.addAll(aList);
                }

                if (!rList.isEmpty()) {
                    restaurantList.addAll(rList);

                }
            } else if (booking.getType().equals(BookingTypeEnum.TOUR)) {
                Tour tour = booking.getTour();
                TourType tourType = tourTypeRepository.getTourTypeTiedToTour(tour.getTour_id());
                tourTypeListOld.add(tourType);
                Attraction attraction = tourService.getAttractionForTourTypeId(tourType.getTour_type_id());

                List<TourType> tourTypeL = attraction.getTour_type_list();
                tourTypeList.addAll(tourTypeL);

            }
        }

        attractionList.removeAll(attractionListOld);
        telecomList.removeAll(telecomListOld);
        accommodationList.removeAll(accommodationListOld);
        tourTypeList.removeAll(tourTypeListOld);

        return new RecommendationResponse(
                attractionList.stream().toList(),
                telecomList.stream().toList(),
                accommodationList.stream().toList(),
                restaurantList.stream().toList(),
                tourTypeList.stream().toList()
        );
    }

}

