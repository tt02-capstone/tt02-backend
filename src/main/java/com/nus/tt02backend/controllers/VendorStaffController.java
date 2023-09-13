package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.services.VendorStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/vendorStaff")
public class VendorStaffController {
    @Autowired
    VendorStaffService vendorStaffService;

    @PutMapping("/vendorStaffLogin/{email}/{password}")
    public ResponseEntity<VendorStaff> vendorStaffLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        VendorStaff vendorStaff = vendorStaffService.vendorStaffLogin(email, password);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping ("/updateVendorStaff")
    public ResponseEntity<Void> vendorStaffLogin(@RequestBody VendorStaff vendorStaffToUpdate) throws NotFoundException {
        vendorStaffService.updateVendorStaff(vendorStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createVendorStaff")
    public ResponseEntity<Long> createVendorStaff(@RequestBody VendorStaff vendorStaffToCreate) throws BadRequestException {
        Long vendorStaffId = vendorStaffService.createVendorStaff(vendorStaffToCreate);
        return ResponseEntity.ok(vendorStaffId);
    }

    @GetMapping("/getAllAssociatedVendorStaff/{vendorId}")
    public ResponseEntity<List<VendorStaff>> getAllVendorStaff(@PathVariable Long vendorId) {
        List<VendorStaff> vendorStaffs = vendorStaffService.getAllAssociatedVendorStaff(vendorId);
        return ResponseEntity.ok(vendorStaffs);
    }

    @PutMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = vendorStaffService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PutMapping ("/passwordResetStageTwo/{token}/{password}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = vendorStaffService.passwordResetStageTwo(token, password);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/getVendorStaffProfile/{vendorStaffId}")
    public ResponseEntity<VendorStaff> getVendorStaffProfile(@PathVariable Long vendorStaffId) throws VendorStaffNotFoundException {
        VendorStaff vendorStaff = vendorStaffService.getVendorStaffProfile(vendorStaffId);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping("/editVendorStaffProfile")
    public ResponseEntity<VendorStaff> editVendorStaffProfile(@RequestBody VendorStaff vendorStaffToEdit) throws EditVendorStaffException {
        System.out.println("tanweekek");
        System.out.println(vendorStaffToEdit);
        System.out.print(vendorStaffToEdit.getUser_id());
        VendorStaff vendorStaff = vendorStaffService.editVendorStaffProfile(vendorStaffToEdit);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping("/editVendorStaffPassword/{staffId}/{oldPassword}/{newPassword}")
    public void editVendorStaffPassword(@PathVariable Long staffId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
        vendorStaffService.editVendorStaffPassword(staffId, oldPassword, newPassword);
    }

    @PutMapping("/toggleBlock/{vendorStaffId}")
    public void vendorStaffLogin(@PathVariable Long vendorStaffId) throws NotFoundException, ToggleBlockException {
        vendorStaffService.toggleBlock(vendorStaffId);
    }
}
