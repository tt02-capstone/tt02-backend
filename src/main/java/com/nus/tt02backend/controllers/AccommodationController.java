package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Room;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.AccommodationService;
import com.nus.tt02backend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/accommodation")
public class AccommodationController {

    @Autowired
    AccommodationService accommodationService;

    @PostMapping("createAccommodation/{vendorStaffId}")
    public ResponseEntity<Accommodation> createAccommodation(@PathVariable Long vendorStaffId , @RequestBody Accommodation accommodationToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        VendorStaff vendorStaff = accommodationService.retrieveVendor(vendorStaffId);
        Accommodation accommodation =  accommodationService.createAccommodation(vendorStaff,accommodationToCreate);
        return ResponseEntity.ok(accommodation);
    }

    @PostMapping("createRoom/{accommodationId}")
    public ResponseEntity<Room> createRoom(@PathVariable Long accommodationId , @RequestBody Room roomToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        Room room =  accommodationService.createRoom(accommodation,roomToCreate);
        return ResponseEntity.ok(room);
    }
}
