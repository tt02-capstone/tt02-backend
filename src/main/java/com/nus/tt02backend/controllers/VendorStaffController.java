package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.services.VendorService;
import com.nus.tt02backend.services.VendorStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/vendorStaff")
public class VendorStaffController {
    @Autowired
    VendorStaffService vendorStaffService;

    @PostMapping("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = vendorStaffService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}/{password}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = vendorStaffService.passwordResetStageTwo(token, password);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping ("/verifyEmail/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token)
            throws BadRequestException {
        String successMessage = vendorStaffService.verifyEmail(token);
        return ResponseEntity.ok(successMessage);
    }
}
