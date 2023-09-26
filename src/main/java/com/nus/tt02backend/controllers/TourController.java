package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Tour;
import com.nus.tt02backend.models.TourType;
import com.nus.tt02backend.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/tour")
public class TourController {
    @Autowired
    TourService tourService;

    @PostMapping("/createTourType/{userId}/{attractionId}")
    public ResponseEntity<TourType> createTourType(@PathVariable Long userId, @PathVariable Long attractionId,
                                                   @RequestBody TourType tourTypeToCreate) throws BadRequestException {
        TourType createdTourType = tourService.createTourType(userId, attractionId, tourTypeToCreate);
        return ResponseEntity.ok(createdTourType);
    }

    @GetMapping("/getAllTourTypesByLocal/{userId}")
    public ResponseEntity<List<TourType>> getAllTourTypesByLocal(@PathVariable Long userId)
            throws BadRequestException {
        List<TourType> tourTypes = tourService.getAllTourTypesByLocal(userId);
        return ResponseEntity.ok(tourTypes);
    }

    @GetMapping("/getTourTypeByTourTypeId/{tourTypeId}")
    public ResponseEntity<TourType> getTourTypeByTourTypeId(@PathVariable Long tourTypeId)
            throws BadRequestException {
        TourType tourType = tourService.getTourTypeByTourTypeId(tourTypeId);
        return ResponseEntity.ok(tourType);
    }

    @PutMapping("/updateTourType")
    public ResponseEntity<TourType> updateTourType(@RequestBody TourType tourTypeToUpdate) throws BadRequestException {
        TourType updatedTourType = tourService.updateTourType(tourTypeToUpdate);
        return ResponseEntity.ok(updatedTourType);
    }

    @DeleteMapping("/deleteTourType/{tourTypeIdToDelete}")
    public ResponseEntity<String> updateTourType(@PathVariable Long tourTypeIdToDelete) throws BadRequestException {
        String responseMessage = tourService.deleteTourType(tourTypeIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getLastTourTypeId")
    public ResponseEntity<?> getLastTourTypeId() {
        try {
            Long lastTourTypeId = tourService.getLastTourTypeId();
            return ResponseEntity.ok(lastTourTypeId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/createTour/{tourTypeId}")
    public ResponseEntity<Long> createTour(@PathVariable Long tourTypeId, @RequestBody Tour tour) throws BadRequestException {
        Long tourId = tourService.createTour(tourTypeId, tour);
        return ResponseEntity.ok(tourId);
    }
}
