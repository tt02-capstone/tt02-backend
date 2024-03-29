package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.Payment;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.services.BookingService;
import com.stripe.exception.StripeException;
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

    @GetMapping("/getAllBookingsByVendor/{vendorStaffId}")
    public ResponseEntity<List<Booking>> getAllBookingsByVendor(@PathVariable Long vendorStaffId) throws NotFoundException {
        List<Booking> bookingList = bookingService.getAllBookingsByVendor(vendorStaffId);
        return ResponseEntity.ok(bookingList);
    }

    @GetMapping("/getBookingByBookingId/{bookingId}")
    public ResponseEntity<Booking> getBookingByBookingId(@PathVariable Long bookingId) throws NotFoundException {
        Booking booking = bookingService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/cancelBooking/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) throws NotFoundException, BadRequestException, StripeException {
        String responseMessage = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/updateBookingItemStatus/{bookingId}")
    public ResponseEntity<Long> updateBookingItemStatus(@PathVariable Long bookingId, @RequestParam BookingStatusEnum status) throws NotFoundException, BadRequestException, StripeException {
        Long bookingNum = bookingService.updateBookingItemStatus(status, bookingId);
        return ResponseEntity.ok(bookingNum);
    }
    @GetMapping("/getAllPaymentsByUser/{userId}")
    public ResponseEntity<List<Payment>> getAllPaymentsByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Payment> paymentList = bookingService.getAllPaymentsByUser(userId);
        return ResponseEntity.ok(paymentList);
    }

    @GetMapping("/getAllItemBookingsByVendor/{vendorId}")
    public ResponseEntity<List<Booking>> getAllItemBookingsByVendor(@PathVariable Long vendorId) throws NotFoundException, BadRequestException {
        List<Booking> itemList = bookingService.getAllItemBookingsByVendor(vendorId);
        return ResponseEntity.ok(itemList);
    }

    @PostMapping("/createTourBooking/{tourId}")
    public ResponseEntity<Long> createTourBooking(@PathVariable Long tourId, @RequestBody Booking booking) throws NotFoundException {
        System.out.println("aaa");
        Long bookingId = bookingService.createTourBooking(tourId, booking); // need to eventually add payment
        return ResponseEntity.ok(bookingId);
    }

    // not to be used by frontend
    @PostMapping("/createAttractionBooking/{attractionId}")
    public ResponseEntity<Booking> createAttractionBooking(@PathVariable Long attractionId, @RequestBody Booking newBooking) throws NotFoundException {
        Booking booking = bookingService.createAttractionBooking(attractionId, newBooking);
        return ResponseEntity.ok(booking);
    }

    // not to be used by frontend
    @PostMapping("/createTelecomBooking/{telecomId}")
    public ResponseEntity<Booking> createTelecomBooking(@PathVariable Long telecomId, @RequestBody Booking newBooking) throws NotFoundException {
        Booking booking = bookingService.createTelecomBooking(telecomId, newBooking);
        return ResponseEntity.ok(booking);
    }

    // not to be used by frontend
    @PostMapping("/createRoomBooking/{roomId}")
    public ResponseEntity<Booking> createRoomBooking(@PathVariable Long roomId, @RequestBody Booking newBooking) throws NotFoundException {
        Booking booking = bookingService.createRoomBooking(roomId, newBooking);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/retrieveAllBookings")
    public ResponseEntity<List<Booking>> retrieveAllBookings() throws NotFoundException {
        List<Booking> bookingList = bookingService.retrieveAllBookings();
        return ResponseEntity.ok(bookingList);
    }

    @GetMapping("/getTourImageByTourId/{tourId}")
    public ResponseEntity<String> getTourImageByTourId(@PathVariable Long tourId) throws BadRequestException {
        String imageLink = bookingService.getTourImageByTourId(tourId);
        return ResponseEntity.ok(imageLink);
    }
}