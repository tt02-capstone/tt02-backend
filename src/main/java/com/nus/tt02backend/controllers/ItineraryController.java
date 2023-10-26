package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Itinerary;
import com.nus.tt02backend.models.Telecom;
import com.nus.tt02backend.services.DIYEventService;
import com.nus.tt02backend.services.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<Telecom> telecomRecommendations = itineraryService.getTelecomRecommendations(itineraryId);
        return ResponseEntity.ok(telecomRecommendations);
    }

    @GetMapping("/getAttractionRecommendations/{itineraryId}")
    public ResponseEntity<List<Attraction>> getAttractionRecommendations(@PathVariable Long itineraryId) throws BadRequestException {
        List<Attraction> attractionRecommendations = itineraryService.getAttractionRecommendations(itineraryId);
        return ResponseEntity.ok(attractionRecommendations);
    }
}
