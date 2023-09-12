package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/vendor")
public class VendorController {
    @Autowired
    VendorService vendorService;

    @PostMapping("/vendorLogin/{email}/{password}")
    public ResponseEntity<VendorStaff> vendorLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        VendorStaff vendorStaff = vendorService.vendorLogin(email, password);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping ("/updateVendor")
    public ResponseEntity<Void> updateVendor(@RequestBody VendorStaff vendorStaffToUpdate) throws NotFoundException {
        vendorService.updateVendor(vendorStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createVendor")
    public ResponseEntity<Long> createVendor(@RequestBody VendorStaff vendorStaffToCreate) throws BadRequestException {
        Long vendorId = vendorService.createVendor(vendorStaffToCreate);
        return ResponseEntity.ok(vendorId);
    }

    @PostMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = vendorService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}/{password}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = vendorService.passwordResetStageTwo(token, password);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping ("/verifyEmail/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token)
            throws BadRequestException {
        String successMessage = vendorService.verifyEmail(token);
        return ResponseEntity.ok(successMessage);
    }
}
