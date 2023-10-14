package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.VendorService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/vendor")
public class VendorController {
    @Autowired
    VendorService vendorService;

    @PostMapping ("/createVendor")
    public ResponseEntity<Long> createVendor(@RequestBody VendorStaff vendorStaffToCreate) throws BadRequestException, StripeException {
        Long vendorId = vendorService.createVendor(vendorStaffToCreate);
        return ResponseEntity.ok(vendorId);
    }

    @GetMapping ("/getAllVendors")
    public ResponseEntity<List<Vendor>> getAllVendors(){
        List<Vendor> vendorList = vendorService.getAllVendors();
        return ResponseEntity.ok(vendorList);
    }

    @PutMapping ("/updateWallet/{vendorId}/{updateAmount}")
    public ResponseEntity<BigDecimal> updateWallet(@PathVariable Long vendorId, @PathVariable BigDecimal updateAmount) throws BadRequestException, NotFoundException {
        BigDecimal updatedWalletAmount = vendorService.updateWallet(vendorId, updateAmount);
        return ResponseEntity.ok(updatedWalletAmount);
    }

    @GetMapping ("/getWithdrawalRequests/{vendorId}")
    public ResponseEntity<BigDecimal> getWithdrawalRequests(@PathVariable Long vendorId) throws BadRequestException, NotFoundException {
        BigDecimal updatedWalletAmount = vendorService.getWithdrawalRequests(vendorId);
        return ResponseEntity.ok(updatedWalletAmount);
    }
}
