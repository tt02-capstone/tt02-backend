package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.ItineraryFriendResponse;
import com.nus.tt02backend.dto.SuggestedEventsResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.services.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/itinerary")
public class ItineraryController {
    @Autowired
    ItineraryService itineraryService;

    @GetMapping("/getItineraryByUser/{userId}")
    public ResponseEntity<Itinerary> getItineraryByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        Itinerary itinerary = itineraryService.getItineraryByUser(userId);
        return ResponseEntity.ok(itinerary);
    }

    @PostMapping("/createItinerary/{userId}")
    public ResponseEntity<Itinerary> createItinerary(@PathVariable Long userId, @RequestBody Itinerary itineraryToCreate) throws BadRequestException {
        Itinerary itinerary = itineraryService.createItinerary(userId, itineraryToCreate);
        return ResponseEntity.ok(itinerary);
    }

    @PutMapping("/updateItinerary/{itineraryId}")
    public ResponseEntity<Itinerary> updateItinerary(@PathVariable Long itineraryId, @RequestBody Itinerary itineraryToUpdate) throws IllegalArgumentException, NotFoundException, BadRequestException {
        Itinerary itinerary = itineraryService.updateItinerary(itineraryId, itineraryToUpdate);
        return ResponseEntity.ok(itinerary);
    }

    @DeleteMapping ("/deleteItinerary/{userId}/{itineraryId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Long userId, @PathVariable Long itineraryId) throws NotFoundException, BadRequestException {
        itineraryService.deleteItinerary(userId, itineraryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getTelecomRecommendations/{itineraryId}")
    public ResponseEntity<List<Telecom>> getTelecomRecommendations(@PathVariable Long itineraryId) throws BadRequestException {
        System.out.println("getTelecomRecommendations HERE!");
        List<Telecom> telecomRecommendations = itineraryService.getTelecomRecommendations(itineraryId);
        return ResponseEntity.ok(telecomRecommendations);
    }

    // Recommendations for Attraction (based on a specific date)
    // Sample GET request for Postman: http://localhost:8080/itinerary/getAttractionRecommendationsByDate/1/2023-10-25
    @GetMapping("/getAttractionRecommendationsByDate/{itineraryId}/{dateTime}")
    public ResponseEntity<List<Attraction>> getAttractionRecommendationsByDate(@PathVariable Long itineraryId, @PathVariable LocalDate dateTime) throws BadRequestException {
        System.out.println("getAttractionRecommendationsByDate HERE!");
        List<Attraction> attractionRecommendations = itineraryService.getAttractionRecommendationsByDate(itineraryId, dateTime);
        return ResponseEntity.ok(attractionRecommendations);
    }

    // Recommendations for Attraction (based on the entire itinerary duration)
    @GetMapping("/getAttractionRecommendationsForItinerary/{itineraryId}")
    public ResponseEntity<List<Attraction>> getAttractionRecommendationsForItinerary(@PathVariable Long itineraryId) throws BadRequestException {
        List<Attraction> attractionRecommendations = itineraryService.getAttractionRecommendationsForItinerary(itineraryId);
        return ResponseEntity.ok(attractionRecommendations);
    }

    // Recommendations for Accommodation (based on the entire itinerary duration)
    @GetMapping("/getAccommodationRecommendationsForItinerary/{itineraryId}")
    public ResponseEntity<List<Accommodation>> getAccommodationRecommendationsForItinerary(@PathVariable Long itineraryId) throws BadRequestException {
        List<Accommodation> accommodationRecommendations = itineraryService.getAccommodationRecommendationsForItinerary(itineraryId);
        return ResponseEntity.ok(accommodationRecommendations);
    }

    // Recommendations for Restaurants (based on a specific date)
    // Sample GET request for Postman: http://localhost:8080/itinerary/getRestaurantRecommendationsForItinerary/1/2023-10-25
    @GetMapping("/getRestaurantRecommendationsForItinerary/{itineraryId}/{dateTime}")
    public ResponseEntity<List<Restaurant>> getRestaurantRecommendationsForItinerary(@PathVariable Long itineraryId,
                                                                                     @PathVariable LocalDate dateTime) throws BadRequestException {
        List<Restaurant> restaurantRecommendations = itineraryService.getRestaurantRecommendationsForItinerary(itineraryId, dateTime);
        return ResponseEntity.ok(restaurantRecommendations);
    }

    @GetMapping("/getSuggestedEventsBasedOnTimeslot/{startTime}/{endTime}")
    public ResponseEntity<SuggestedEventsResponse> getSuggestedEventsBasedOnTimeslot(@PathVariable LocalTime startTime,
                                                                                     @PathVariable LocalTime endTime) throws BadRequestException {
        SuggestedEventsResponse suggestedEvents = itineraryService.getSuggestedEventsBasedOnTimeslot(startTime, endTime);
        return ResponseEntity.ok(suggestedEvents);
    }

    @GetMapping("/existingAccommodationInItinerary/{itineraryId}")
    public ResponseEntity<Boolean> existingAccommodationInItinerary(@PathVariable Long itineraryId) throws BadRequestException {
        Boolean existingAccommodation = itineraryService.existingAccommodationInItinerary(itineraryId);
        return ResponseEntity.ok(existingAccommodation);
    }

    @GetMapping("/existingTelecomInItinerary/{itineraryId}")
    public ResponseEntity<Boolean> existingTelecomInItinerary(@PathVariable Long itineraryId) throws BadRequestException {
        Boolean existingTelecom = itineraryService.existingTelecomInItinerary(itineraryId);
        return ResponseEntity.ok(existingTelecom);
    }

    @GetMapping("/getUserWithEmailSimilarity/{masterUserId}/{itineraryId}/{email}")
    public ResponseEntity<List<ItineraryFriendResponse>> getUserWithEmailSimilarity(@PathVariable Long masterUserId, @PathVariable Long itineraryId,  @PathVariable String email) throws NotFoundException {
        List<ItineraryFriendResponse> list = itineraryService.getUserWithEmailSimilarity(masterUserId, itineraryId, email);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getInvitedUsers/{itineraryId}")
    public ResponseEntity<List<ItineraryFriendResponse>> getInvitedUsers(@PathVariable Long itineraryId) throws NotFoundException {
        List<ItineraryFriendResponse> list = itineraryService.getInvitedUsers(itineraryId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getAcceptedUsers/{itineraryId}")
    public ResponseEntity<List<ItineraryFriendResponse>> getAcceptedUsers(@PathVariable Long itineraryId) throws NotFoundException {
        List<ItineraryFriendResponse> list = itineraryService.getAcceptedUsers(itineraryId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/toggleItineraryInvite/{itineraryId}/{userIdToAddOrRemove}")
    public ResponseEntity<String> toggleItineraryInvite(@PathVariable Long itineraryId, @PathVariable Long userIdToAddOrRemove) throws NotFoundException {
        String string = itineraryService.toggleItineraryInvite(itineraryId, userIdToAddOrRemove);
        return ResponseEntity.ok(string);
    }

    @GetMapping("/getProfileImageByIdList/{itineraryId}")
    public ResponseEntity<List<String>> getProfileImageByIdList(@PathVariable Long itineraryId) throws NotFoundException {
        List<String> profilePicList = itineraryService.getProfileImageByIdList(itineraryId);
        return ResponseEntity.ok(profilePicList);
    }

    @GetMapping("/getInvitationsByUser/{userId}")
    public ResponseEntity<List<Itinerary>> getInvitationsByUser(@PathVariable Long userId) {
        List<Itinerary> list = itineraryService.getInvitationsByUser(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/addUserToItinerary/{itineraryId}/{userId}")
    public ResponseEntity<String> addUserToItinerary(@PathVariable Long itineraryId, @PathVariable Long userId) throws NotFoundException {
        String string = itineraryService.addUserToItinerary(itineraryId, userId);
        return ResponseEntity.ok(string);
    }

    @PostMapping("/removeUserFromItinerary/{itineraryId}/{userId}")
    public ResponseEntity<String> removeUserFromItinerary(@PathVariable Long itineraryId, @PathVariable Long userId) throws NotFoundException {
        String string = itineraryService.removeUserFromItinerary(itineraryId, userId);
        return ResponseEntity.ok(string);
    }

    @GetMapping("/getItineraryMasterUserEmail/{userId}")
    public ResponseEntity<ItineraryFriendResponse> getItineraryMasterUserEmail(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        ItineraryFriendResponse response = itineraryService.getItineraryMasterUserEmail(userId);
        return ResponseEntity.ok(response);
    }
}
