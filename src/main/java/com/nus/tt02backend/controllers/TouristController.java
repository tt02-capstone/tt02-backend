package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.TouristService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/tourist")
public class TouristController {
    @Autowired
    TouristService touristService;

    @PutMapping ("/update")
    public ResponseEntity<Void> updateTourist(@RequestBody Tourist touristToUpdate) throws NotFoundException {
        touristService.updateTourist(touristToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createTourist(@RequestBody Tourist touristToCreate) throws BadRequestException {
        Long touristId = touristService.createTourist(touristToCreate);
        return ResponseEntity.ok(touristId);
    }

    @PutMapping("/editTouristProfile")
    @PreAuthorize("hasRole('TOURIST') ")
    public ResponseEntity<Tourist> editTouristProfile(@RequestBody Tourist touristToEdit) throws EditUserException {
        Tourist tourist = touristService.editTouristProfile(touristToEdit);
        return ResponseEntity.ok(tourist);
    }

    @GetMapping("/getAllTourist")
    @PreAuthorize("hasRole('TOURIST') ")
    public ResponseEntity<List<Tourist>> getAllTourist() {
        List<Tourist> touristList = touristService.retrieveAllTourist();
        return ResponseEntity.ok(touristList);
    }
}
