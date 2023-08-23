package com.nus.tt02backend.controllers;

import com.nus.tt02backend.models.Staff;
import com.nus.tt02backend.services.impl.StaffServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    StaffServiceImpl staffService;

    @PostMapping("/staffLogin/{email}/{password}")
    public ResponseEntity<Staff> staffLogin(@PathVariable String email, @PathVariable String password) {
        try {
            Staff staff = staffService.staffLogin(email, password);
            return ResponseEntity.ok(staff);
        } catch (EntityNotFoundException exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping ("/updateStaff")
    public ResponseEntity<Void> staffLogin(@RequestBody Staff staffToUpdate) {
        try {
            staffService.updateStaff(staffToUpdate);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody Staff staffToCreate) {
        try {
            Long staffId = staffService.createStaff(staffToCreate);
            return ResponseEntity.ok(staffId);
        } catch (IllegalArgumentException exception) {
            return new ResponseEntity(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
