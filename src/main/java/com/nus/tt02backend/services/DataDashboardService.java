package com.nus.tt02backend.services;


import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DataDashboardService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingItemRepository bookingItemRepository;
    @Autowired
    CartBookingRepository cartBookingRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    TicketPerDayRepository ticketPerDayRepository;
    @Autowired
    AttractionService attractionService;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    TelecomRepository telecomRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    TourRepository tourRepository;

    public List<Object[]> getData(String vendorId) throws NotFoundException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();

            List<Accommodation> accommodations = vendor.getAccommodation_list();

            LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

            LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

            List<Object[]> data = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

            return data;
        } else {
            throw new NotFoundException("Vendor not found");
        }

    }
}
