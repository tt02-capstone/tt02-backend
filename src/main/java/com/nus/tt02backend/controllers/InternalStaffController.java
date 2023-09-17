package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.services.AuthenticationService;
import com.nus.tt02backend.services.InternalStaffService;
import com.nus.tt02backend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class InternalStaffController {
    @Autowired
    InternalStaffService internalStaffService;

    @Autowired
    AuthenticationService authenticationService;
    @PostMapping("/staffLogin/{email}/{password}")
    public ResponseEntity<JwtAuthenticationResponse> staffLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        JwtAuthenticationResponse response = authenticationService.staffLogin(email, password);
        return ResponseEntity.ok(response);
    }

    @PutMapping ("/updateStaff")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<Void> updateStaff(@RequestBody InternalStaff internalStaffToUpdate) throws NotFoundException {
        internalStaffService.updateStaff(internalStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    // not initial sign up
    @PostMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody InternalStaff internalStaffToCreate) throws BadRequestException {
        Long staffId = internalStaffService.createStaff(internalStaffToCreate);
        return ResponseEntity.ok(staffId);
    }

    @GetMapping("/getAllAdmin")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<List<InternalStaff>> getAllAdmin() {
        List<InternalStaff> internalStaffList = internalStaffService.retrieveAllAdmin();
        return ResponseEntity.ok(internalStaffList);
    }

    @PutMapping("/editProfile")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<InternalStaff> editProfile(@RequestBody InternalStaff staffToEdit) throws EditAdminException {
        InternalStaff internalStaff = internalStaffService.editProfile(staffToEdit);
        return ResponseEntity.ok(internalStaff);
    }

    @PostMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = internalStaffService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{email}/{token}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String email, @PathVariable String token)
            throws BadRequestException {
        String successMessage = internalStaffService.passwordResetStageTwo(email, token);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageThree/{email}/{password}")
    public ResponseEntity<String> passwordResetStageThree(@PathVariable String email, @PathVariable String password)
            throws BadRequestException {
        String successMessage = internalStaffService.passwordResetStageThree(email, password);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping ("/getPendingApplications")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<List<Vendor>> getPendingApplications() {
        List<Vendor> vendors = internalStaffService.getPendingApplications();
        return ResponseEntity.ok(vendors);
    }

    @PutMapping ("/updateApplicationStatus/{vendorId}/{applicationStatus}")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<String> updateApplicationStatus(@PathVariable Long vendorId,
                                                          @PathVariable ApplicationStatusEnum applicationStatus)
            throws NotFoundException {
        String successMessage = internalStaffService.updateApplicationStatus(vendorId, applicationStatus);
        return ResponseEntity.ok(successMessage);
    }
}
