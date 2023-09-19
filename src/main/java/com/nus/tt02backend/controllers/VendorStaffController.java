package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.services.VendorService;
import com.nus.tt02backend.services.VendorStaffService;
import com.stripe.exception.StripeException;
import com.stripe.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/vendorStaff")
public class VendorStaffController {
    @Autowired
    VendorStaffService vendorStaffService;

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

    @PutMapping("/editVendorStaffProfile")
    public ResponseEntity<VendorStaff> editVendorStaffProfile(@RequestBody VendorStaff vendorStaffToEdit) throws EditVendorStaffException {
        VendorStaff vendorStaff = vendorStaffService.editVendorStaffProfile(vendorStaffToEdit);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping("/toggleBlock/{vendorStaffId}")
    public void toggleBlock(@PathVariable Long vendorStaffId) throws NotFoundException, ToggleBlockException {
        vendorStaffService.toggleBlock(vendorStaffId);
    }

    @GetMapping ("/verifyEmail/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token)
            throws BadRequestException {
        String successMessage = vendorStaffService.verifyEmail(token);
        return ResponseEntity.ok(successMessage);
    }

    @GetMapping("/getAllVendorStaff")
//    @PreAuthorize("hasRole('VENDOR_STAFF') or hasRole('INTERNAL_STAFF')")
    public ResponseEntity<List<VendorStaff>> getAllVendorStaff() {
        List<VendorStaff> vendorStaffList = vendorStaffService.retrieveAllVendorStaff();
        return ResponseEntity.ok(vendorStaffList);
    }

    @PostMapping("/addBankAccount/{userId}/{token}")
    public ResponseEntity<String> addBankAccount(@PathVariable Long userId, @PathVariable String token) throws StripeException, NotFoundException {
        String bankAccountId = vendorStaffService.addBankAccount(userId, token);

        return ResponseEntity.ok(bankAccountId);
    }

    @GetMapping("/getBankAccounts/{userId}")
    public ResponseEntity<List<ExternalAccount>> getBankAccounts(@PathVariable Long userId) throws StripeException, NotFoundException {
        List<ExternalAccount> bankAccounts = vendorStaffService.getBankAccounts(userId);
        return ResponseEntity.ok(bankAccounts);
    }
}
