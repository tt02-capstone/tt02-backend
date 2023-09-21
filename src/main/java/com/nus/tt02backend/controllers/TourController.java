package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Tour;
import com.nus.tt02backend.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin
@RequestMapping("/tour")
public class TourController {
    @Autowired
    TourService tourService;

    @PostMapping("/createTour/{tourTypeId}")
    public ResponseEntity<Long> createTour(@PathVariable Long tourTypeId, @RequestBody Tour tour) throws BadRequestException {
        Long tourId = tourService.createTour(tourTypeId, tour);
        return ResponseEntity.ok(tourId);
    }
}
