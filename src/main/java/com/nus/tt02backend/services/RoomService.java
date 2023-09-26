package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    



}
