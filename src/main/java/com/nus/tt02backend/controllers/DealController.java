package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.Deal;
import com.nus.tt02backend.models.Room;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.AccommodationService;
import com.nus.tt02backend.services.DealService;
import com.nus.tt02backend.services.VendorStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/deal")
public class DealController {

    @Autowired
    DealService dealService;

    @GetMapping("{vendorStaffId}")
    public ResponseEntity<List<Deal>> getDealsbyVendor(@PathVariable Long vendorStaffId )
            throws BadRequestException, IllegalArgumentException, NotFoundException {
        List<Deal> dealList =  dealService.retrieveAllDealsByVendor(vendorStaffId);
        return ResponseEntity.ok(dealList);
    }

    @PostMapping("create/{vendorStaffId}")
    public ResponseEntity<Deal> createAccommodation(@PathVariable Long vendorStaffId , @RequestBody Deal dealToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        VendorStaff vendorStaff = dealService.retrieveVendor(vendorStaffId);
        Deal newdeal =  dealService.createDeal(vendorStaff, dealToCreate);
        return ResponseEntity.ok(newdeal);
    }

}
