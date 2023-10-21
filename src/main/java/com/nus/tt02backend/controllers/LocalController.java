package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.AuthenticationService;
import com.nus.tt02backend.services.LocalService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/local")
public class LocalController {

    @Autowired
    LocalService localService;
    @Autowired
    AuthenticationService authenticationService;

    @PutMapping("/editLocalProfile")
    @PreAuthorize("hasRole('LOCAL') ")
    public ResponseEntity<JwtAuthenticationResponse> editLocalProfile(@RequestBody Local localToEdit) throws BadRequestException {
        JwtAuthenticationResponse local = authenticationService.editLocalProfile(localToEdit);
        return ResponseEntity.ok(local);
    }

    @PutMapping ("/update")
    @PreAuthorize("hasRole('LOCAL') ")
    public ResponseEntity<Void> updateLocal(@RequestBody Local localToUpdate) throws NotFoundException {
        localService.updateLocal(localToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createLocal(@RequestBody Local localToCreate) throws BadRequestException, StripeException {
        Long localId = localService.createLocal(localToCreate);
        return ResponseEntity.ok(localId);
    }

    @GetMapping("/getAllLocal")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<List<Local>> getAllLocal() {
        List<Local> locaList = localService.retrieveAllLocal();
        return ResponseEntity.ok(locaList);
    }

    @PutMapping ("/updateWallet/{localId}/{updateAmount}")
    public ResponseEntity<BigDecimal> updateWallet(@PathVariable Long localId, @PathVariable BigDecimal updateAmount) throws BadRequestException, NotFoundException, StripeException {
        BigDecimal updatedWalletAmount = localService.updateWallet(localId, updateAmount);
        return ResponseEntity.ok(updatedWalletAmount);
    }

    @GetMapping ("/getWalletHistory/{localId}")
    public ResponseEntity<List<HashMap<String, Object>>> getWithdrawalRequests(@PathVariable Long localId) throws  NotFoundException, StripeException {
        List<HashMap<String, Object>> transactions = localService.getWithdrawalRequests(localId);
        return ResponseEntity.ok(transactions);
    }
}