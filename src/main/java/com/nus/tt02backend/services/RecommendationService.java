package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
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

    public List<Object> getRecommendation(GenericLocationEnum location, ListingTypeEnum listingType, Long typeId) throws NotFoundException {

        List<Object> combinedList = new ArrayList<>();

        if (listingType == ListingTypeEnum.ATTRACTION) {
            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location, typeId);
            List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(location);
            if (!aList.isEmpty() || !rList.isEmpty()) {
                combinedList.addAll(aList);
                combinedList.addAll(rList);

                Collections.shuffle(combinedList, new Random()); // anyhow shuffle so it wont return the same things
                return combinedList.subList(0,3);
            } else {
                return new ArrayList<>(); // return by blank if there is nth ard
            }

        } else if (listingType ==  ListingTypeEnum.RESTAURANT) {
            List<Attraction> aList = attractionService.nearbyAttrRecommendation(location);
            List<Restaurant> rList = restaurantService.nearbyRestaurantRecommendation(location,typeId);

            if (!aList.isEmpty() || !rList.isEmpty()) {
                combinedList.addAll(aList);
                combinedList.addAll(rList);

                Collections.shuffle(combinedList, new Random());
                return combinedList.subList(0,3);
            } else {
                return new ArrayList<>();
            }

        } else {
            // will add in accoms ltr
            throw new NotFoundException("No recommendations to return.");
        }
    }
}
