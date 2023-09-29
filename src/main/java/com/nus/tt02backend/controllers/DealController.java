package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
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

    @GetMapping("/getVendorDealList/{vendorId}")
    public ResponseEntity<List<Deal>> getDealsbyVendor(@PathVariable Long vendorId )
            throws BadRequestException, IllegalArgumentException, NotFoundException {
        List<Deal> dealList =  dealService.retrieveAllDealsByVendor(vendorId);
        return ResponseEntity.ok(dealList);
    }

    @PostMapping("create/{vendorId}")
    public ResponseEntity<Deal> createAccommodation(@PathVariable Long vendorId , @RequestBody Deal dealToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        Vendor vendor = dealService.retrieveVendor(vendorId);
        Deal newdeal =  dealService.createDeal(vendor, dealToCreate);
        return ResponseEntity.ok(newdeal);
    }

    @GetMapping("/getAllDealList")
    public ResponseEntity<List<Deal>> getAllDealeList() {
        List<Deal> dealList = dealService.getAllDealList();
        return ResponseEntity.ok(dealList);
    }


    @GetMapping("/getDealById/{dealId}")
    public ResponseEntity<Deal> getDealById(@PathVariable Long dealId) throws NotFoundException {
        Deal deal = dealService.getDealById(dealId);
        return ResponseEntity.ok(deal);
    }

    @PutMapping("/update")
    public ResponseEntity<Deal> update(@RequestBody Deal dealToEdit) throws NotFoundException {
        Deal deal = dealService.update(dealToEdit);
        return ResponseEntity.ok(deal);
    }

    @GetMapping("/getLastDealId")
    public ResponseEntity<?> getLastDealId() {
        try {
            Long lastDealId = dealService.getLastDealId();
            return ResponseEntity.ok(lastDealId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}
