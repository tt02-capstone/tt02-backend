package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.Payment;
import com.nus.tt02backend.services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    BookingService bookingService;

    @GetMapping("/getAllBookingsByUser/{userId}")
    public ResponseEntity<List<Booking>> getAllBookingsByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Booking> bookingList = bookingService.getAllBookingsByUser(userId);
        return ResponseEntity.ok(bookingList);
    }

    @GetMapping("/getAllAttractionBookingsByVendor/{vendorStaffId}")
    public ResponseEntity<List<Booking>> getAllAttractionBookingsByVendor(@PathVariable Long vendorStaffId) throws NotFoundException, BadRequestException {
        List<Booking> bookingList = bookingService.getAllAttractionBookingsByVendor(vendorStaffId);
        return ResponseEntity.ok(bookingList);
    }

    @GetMapping("/getAttractionBookingByVendor/{vendorStaffId}/{bookingId}")
    public ResponseEntity<Booking> getAttractionBookingByVendor(@PathVariable Long vendorStaffId, @PathVariable Long bookingId) throws NotFoundException {
        Booking booking = bookingService.getAttractionBookingByVendor(vendorStaffId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/getBookingByBookingId/{bookingId}")
    public ResponseEntity<Booking> getBookingByBookingId(@PathVariable Long bookingId) throws NotFoundException {
        Booking booking = bookingService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/cancelBooking/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) throws NotFoundException, BadRequestException {
        String responseMessage = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getAllPaymentsByUser/{userId}")
    public ResponseEntity<List<Payment>> getAllPaymentsByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Payment> paymentList = bookingService.getAllPaymentsByUser(userId);
        return ResponseEntity.ok(paymentList);
    }

    @PostMapping("/createTourBooking/{tourId}")
    public ResponseEntity<Long> createTourBooking(@PathVariable Long tourId, @RequestBody Booking booking) throws NotFoundException {
        System.out.println("aaa");
        Long bookingId = bookingService.createTourBooking(tourId, booking); // need to eventually add payment
        return ResponseEntity.ok(bookingId);
    }

    // To be deleted - for testing purposes
    @GetMapping("/tempCreateBooking")
    public ResponseEntity<String> tempCreateBooking() throws NotFoundException {
        String responseMessage = bookingService.tempCreateBooking();
        return ResponseEntity.ok(responseMessage);
    }
}