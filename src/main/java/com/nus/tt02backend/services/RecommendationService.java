package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
import com.nus.tt02backend.repositories.AccommodationRepository;
import com.nus.tt02backend.repositories.AttractionRepository;
import com.nus.tt02backend.repositories.CategoryItemRepository;
import com.nus.tt02backend.repositories.RestaurantRepository;
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
}
