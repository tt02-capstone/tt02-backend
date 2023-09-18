package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.PaymentService;
import com.nus.tt02backend.services.VendorService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @GetMapping("/getVendorTotalEarnings/{vendorId}")
    public ResponseEntity<BigDecimal> getVendorTotalEarnings(@PathVariable Long vendorId) throws BadRequestException {
        BigDecimal sum = paymentService.getVendorTotalEarnings(vendorId);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/getTourTotalEarningForLocal/{localId}")
    public ResponseEntity<BigDecimal> getTourTotalEarningForLocal(@PathVariable Long localId) {
        BigDecimal sum = paymentService.getTourTotalEarningForLocal(localId);
        return ResponseEntity.ok(sum);
    }
}
