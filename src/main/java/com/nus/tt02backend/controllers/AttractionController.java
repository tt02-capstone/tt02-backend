package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/attraction")
public class AttractionController {
    @Autowired
    AttractionService attractionService;

    @GetMapping("/getAllAttraction")
    public ResponseEntity<List<Attraction>> getAttractionList() {
        List<Attraction> attractionList = attractionService.retrieveAllAttraction();
        return ResponseEntity.ok(attractionList);
    }

    @GetMapping("/getAttraction/{attractionId}")
    public ResponseEntity<Attraction> getAttraction(@PathVariable Long attractionId) throws NotFoundException {
        Attraction attraction = attractionService.retrieveAttraction(attractionId);
        return ResponseEntity.ok(attraction);
    }


    @PostMapping ("createAttraction/{vendorStaffId}")
    public ResponseEntity<Attraction> createAttraction(@PathVariable Long vendorStaffId ,@RequestBody Attraction attractionToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException  {

        VendorStaff vendorStaff = attractionService.retrieveVendor(vendorStaffId);
        Attraction attraction =  attractionService.createAttraction(vendorStaff,attractionToCreate);
        return ResponseEntity.ok(attraction);
    }

    @PutMapping("/updateAttraction/{vendorStaffId}")
    public ResponseEntity<Void> updateAttraction(@PathVariable Long vendorStaffId ,@RequestBody Attraction attractionToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = attractionService.retrieveVendor(vendorStaffId);
        attractionService.updateAttraction(vendorStaff, attractionToUpdate);
        return ResponseEntity.noContent().build();
    }

}
