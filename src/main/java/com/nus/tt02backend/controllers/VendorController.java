package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.services.impl.VendorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/vendor")
public class VendorController {
    @Autowired
    VendorServiceImpl vendorService;

    @PostMapping("/vendorLogin/{email}/{password}")
    public ResponseEntity<Vendor> vendorLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        Vendor vendor = vendorService.vendorLogin(email, password);
        return ResponseEntity.ok(vendor);
    }

    @PutMapping ("/updateVendor")
    public ResponseEntity<Void> vendorLogin(@RequestBody Vendor vendorToUpdate) throws NotFoundException {
        vendorService.updateVendor(vendorToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createVendor")
    public ResponseEntity<Long> createVendor(@RequestBody Vendor vendorToCreate) throws BadRequestException {
        Long vendorId = vendorService.createVendor(vendorToCreate);
        return ResponseEntity.ok(vendorId);
    }
}
