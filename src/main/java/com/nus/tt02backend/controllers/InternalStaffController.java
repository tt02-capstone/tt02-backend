package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.services.InternalStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody InternalStaff internalStaffToCreate) throws BadRequestException {
        Long staffId = internalStaffService.createStaff(internalStaffToCreate);
        return ResponseEntity.ok(staffId);
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
