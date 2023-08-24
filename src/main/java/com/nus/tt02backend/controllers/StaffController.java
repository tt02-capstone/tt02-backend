package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exception.BadRequestException;
import com.nus.tt02backend.exception.NotFoundException;
import com.nus.tt02backend.models.Staff;
import com.nus.tt02backend.services.impl.StaffServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff")
public class StaffController {
    @Autowired
    StaffServiceImpl staffService;

    @PostMapping("/staffLogin/{email}/{password}")
    public ResponseEntity<Staff> staffLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        Staff staff = staffService.staffLogin(email, password);
        return ResponseEntity.ok(staff);
    }

    @PutMapping ("/updateStaff")
    public ResponseEntity<Void> staffLogin(@RequestBody Staff staffToUpdate) throws NotFoundException {
        staffService.updateStaff(staffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createStaff")
    public ResponseEntity<Long> createStaff(@RequestBody Staff staffToCreate) throws BadRequestException {
        Long staffId = staffService.createStaff(staffToCreate);
        return ResponseEntity.ok(staffId);
    }
}
