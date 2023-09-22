package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.services.AuthenticationService;
import com.nus.tt02backend.services.VendorService;
import com.nus.tt02backend.services.VendorStaffService;
import com.stripe.exception.StripeException;
import com.stripe.model.ExternalAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/vendorStaff")
public class VendorStaffController {
    @Autowired
    VendorStaffService vendorStaffService;
    @Autowired
    AuthenticationService authenticationService;

    @PutMapping ("/updateVendorStaff")
    public ResponseEntity<Void> vendorStaffLogin(@RequestBody VendorStaff vendorStaffToUpdate) throws NotFoundException {
        vendorStaffService.updateVendorStaff(vendorStaffToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/createVendorStaff")
    @PreAuthorize("hasRole('VENDOR_ADMIN')")
    public ResponseEntity<Long> createVendorStaff(@RequestBody VendorStaff vendorStaffToCreate) throws BadRequestException {
        Long vendorStaffId = vendorStaffService.createVendorStaff(vendorStaffToCreate);
        return ResponseEntity.ok(vendorStaffId);
    }

    @GetMapping("/getAllAssociatedVendorStaff/{vendorId}")
    @PreAuthorize("hasRole('INTERNAL_STAFF') or hasRole('VENDOR_STAFF') or hasRole('VENDOR_ADMIN')")
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
    @PreAuthorize("hasRole('VENDOR_STAFF') or hasRole('VENDOR_ADMIN')")
    public ResponseEntity<JwtAuthenticationResponse> editVendorStaffProfile(@RequestBody VendorStaff vendorStaffToEdit) throws BadRequestException {
        JwtAuthenticationResponse vendorStaff = authenticationService.editVendorStaffProfile(vendorStaffToEdit);
        return ResponseEntity.ok(vendorStaff);
    }

    @PutMapping("/toggleBlock/{vendorStaffId}")
    @PreAuthorize("hasRole('VENDOR_ADMIN')")
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
    @PreAuthorize("hasRole('INTERNAL_STAFF') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_STAFF')")
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

    @PutMapping("/deleteBankAccount/{userId}/{bank_account_id}")
    public ResponseEntity<String> deleteBankAccount(@PathVariable Long userId, @PathVariable String bank_account_id) throws StripeException, NotFoundException {
        String deletedBankAccountId = vendorStaffService.deleteBankAccount(userId, bank_account_id);

        return ResponseEntity.ok(deletedBankAccountId);
    }

    @PostMapping("/withdrawWallet/{userId}/{bank_account_id}/{amount}")
    public ResponseEntity<String> withdrawWallet(@PathVariable Long userId, @PathVariable String bank_account_id,
                                                 @PathVariable BigDecimal amount) throws StripeException, NotFoundException {
        String payOutId = vendorStaffService.withdrawWallet(userId, bank_account_id, amount);

        return ResponseEntity.ok(payOutId);
    }

    @PostMapping("/topUpWallet/{userId}/{amount}")
    public ResponseEntity<String> topUpWallet(@PathVariable Long userId, @PathVariable BigDecimal amount) throws StripeException, NotFoundException {
        String chargeId = vendorStaffService.topUpWallet(userId, amount);

        return ResponseEntity.ok(chargeId );
    }
}