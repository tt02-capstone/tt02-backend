package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.services.impl.StaffServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    StaffServiceImpl staffService;

    @PostMapping("/staffLogin/{email}/{password}")
    public ResponseEntity<InternalStaff> staffLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        InternalStaff internalStaff = staffService.staffLogin(email, password);
        return ResponseEntity.ok(internalStaff);
    }

    @PutMapping ("/updateStaff")
    public ResponseEntity<Void> staffLogin(@RequestBody InternalStaff internalStaffToUpdate) throws NotFoundException {
        staffService.updateStaff(internalStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody InternalStaff internalStaffToCreate) throws BadRequestException {
        Long staffId = staffService.createStaff(internalStaffToCreate);
        return ResponseEntity.ok(staffId);
    }
}
