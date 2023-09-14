package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/vendor")
public class VendorController {
    @Autowired
    VendorService vendorService;

    @PostMapping ("/createVendor")
    public ResponseEntity<Long> createVendor(@RequestBody VendorStaff vendorStaffToCreate) throws BadRequestException {
        Long vendorId = vendorService.createVendor(vendorStaffToCreate);
        return ResponseEntity.ok(vendorId);
    }
}
