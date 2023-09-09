package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.services.InternalStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/staff")
public class InternalStaffController {
    @Autowired
    InternalStaffService internalStaffService;

    @PostMapping("/staffLogin/{email}/{password}")
    public ResponseEntity<InternalStaff> staffLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        InternalStaff internalStaff = internalStaffService.staffLogin(email, password);
        return ResponseEntity.ok(internalStaff);
    }

    @PutMapping ("/updateStaff")
    public ResponseEntity<Void> staffLogin(@RequestBody InternalStaff internalStaffToUpdate) throws NotFoundException {
        internalStaffService.updateStaff(internalStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PutMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody InternalStaff internalStaffToCreate) throws BadRequestException {
        Long staffId = internalStaffService.createStaff(internalStaffToCreate);
        return ResponseEntity.ok(staffId);
    }

    @GetMapping("/getAllStaff")
    public ResponseEntity<List<InternalStaff>> getAllStaff() {
        List<InternalStaff> internalStaffList = internalStaffService.retrieveAllStaff();
        return ResponseEntity.ok(internalStaffList);
    }

    @GetMapping("/getStaffProfile/{staffId}")
    public ResponseEntity<InternalStaff> getStaffProfile(@PathVariable Long staffId) throws AdminNotFoundException {
        InternalStaff internalStaff = internalStaffService.getStaffProfile(staffId);
        return ResponseEntity.ok(internalStaff);
    }

    @PostMapping("/editAdminProfile")
    public ResponseEntity<InternalStaff> editStaffProfile(@RequestBody InternalStaff staffToEdit) throws EditAdminException {
        InternalStaff internalStaff = internalStaffService.editStaffProfile(staffToEdit);
        return ResponseEntity.ok(internalStaff);
    }

    @PostMapping("/editAdminPassword/{staffId}/{oldPassword}/{newPassword}")
    public void editStaffPassword(@PathVariable Long staffId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
        internalStaffService.editStaffPassword(staffId, oldPassword, newPassword);
    }

    @PostMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = internalStaffService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}/{password}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = internalStaffService.passwordResetStageTwo(token, password);
        return ResponseEntity.ok(successMessage);
    }
}
