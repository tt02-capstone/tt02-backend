package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.Tour;
import com.nus.tt02backend.models.TourType;
import com.nus.tt02backend.services.TourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    @GetMapping("/getAllTourTypesCreated")
    public ResponseEntity<List<TourType>> getAllTourTypesCreated() {
        List<TourType> tourTypes = tourService.getAllTourTypesCreated();
        return ResponseEntity.ok(tourTypes);
    }

    @PutMapping("/updateTourType/{attractionId}")
    public ResponseEntity<TourType> updateTourType(@PathVariable Long attractionId, @RequestBody TourType tourTypeToUpdate)
            throws BadRequestException {
        TourType updatedTourType = tourService.updateTourType(attractionId, tourTypeToUpdate);
        return ResponseEntity.ok(updatedTourType);
    }

    @PutMapping("/adminUpdateTourType/{tourTypeIdToUpdate}/{newPublishedStatus}")
    public ResponseEntity<TourType> adminUpdateTourType(@PathVariable Long tourTypeIdToUpdate, @PathVariable Boolean newPublishedStatus)
            throws BadRequestException {
        TourType updatedTourType = tourService.adminUpdateTourType(tourTypeIdToUpdate, newPublishedStatus);
        return ResponseEntity.ok(updatedTourType);
    }

    @DeleteMapping("/deleteTourType/{tourTypeIdToDelete}")
    public ResponseEntity<String> deleteTourType(@PathVariable Long tourTypeIdToDelete) throws BadRequestException {
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

    @GetMapping("/getAttractionForTourTypeId/{tourTypeId}")
    public ResponseEntity<Attraction> getAttractionForTourTypeId(@PathVariable Long tourTypeId)
            throws BadRequestException {
        Attraction attraction = tourService.getAttractionForTourTypeId(tourTypeId);
        return ResponseEntity.ok(attraction);
    }

    @PostMapping("/createTour/{tourTypeId}")
    public ResponseEntity<Tour> createTour(@PathVariable Long tourTypeId,
                                           @RequestBody Tour tourToCreate) throws BadRequestException {
        Tour createdTour = tourService.createTour(tourTypeId, tourToCreate);
        return ResponseEntity.ok(createdTour);
    }

    @GetMapping("/getAllToursByTourType/{tourTypeId}")
    public ResponseEntity<List<Tour>> getAllToursByTourType(@PathVariable Long tourTypeId) throws BadRequestException {
        List<Tour> tours = tourService.getAllToursByTourType(tourTypeId);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/getTourByTourId/{tourId}")
    public ResponseEntity<Tour> getTourByTourId(@PathVariable Long tourId) throws BadRequestException {
        Tour tour = tourService.getTourByTourId(tourId);
        return ResponseEntity.ok(tour);
    }

    @PutMapping("/updateTour")
    public ResponseEntity<Tour> updateTour(@RequestBody Tour tourToUpdate)
            throws BadRequestException {
        Tour updatedTour = tourService.updateTour(tourToUpdate);
        return ResponseEntity.ok(updatedTour);
    }

    @DeleteMapping("/deleteTour/{tourIdToDelete}")
    public ResponseEntity<String> deleteTour(@PathVariable Long tourIdToDelete) throws BadRequestException {
        String responseMessage = tourService.deleteTour(tourIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getAllTourTypesByAttraction/{attractionId}/{dateSelected}")
    public ResponseEntity<List<TourType>> getAllTourTypesByAttraction(@PathVariable Long attractionId,
                                                                      @PathVariable String dateSelected)
            throws BadRequestException, ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
        Date date = dateFormat.parse(dateSelected);
        Instant instant = date.toInstant();
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

        List<TourType> tourTypes = tourService.getAllTourTypesByAttraction(attractionId, localDateTime);
        return ResponseEntity.ok(tourTypes);
    }

    @GetMapping("/getAllBookingsByLocal/{userId}")
    public ResponseEntity<List<Booking>> getAllBookingsByLocal(@PathVariable Long userId) throws BadRequestException {
        List<Booking> bookings = tourService.getAllBookingsByLocal(userId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/getBookingByBookingId/{bookingId}")
    public ResponseEntity<Booking> getBookingByBookingId(@PathVariable Long bookingId) throws BadRequestException {
        Booking booking = tourService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(booking);
    }

    /*
    @PostMapping("/createTour/{tourTypeId}")
    public ResponseEntity<Long> createTour(@PathVariable Long tourTypeId, @RequestBody Tour tour) throws BadRequestException {
        Long tourId = tourService.createTour(tourTypeId, tour);
        return ResponseEntity.ok(tourId);
    }
    */
}
