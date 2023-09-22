package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.ExternalAccount;
import com.stripe.param.CustomerCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.PaymentMethod;

import java.util.*;
import java.math.BigDecimal;

@RestController
@CrossOrigin
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/createStripeCustomer/{tourist_email}/{tourist_name}")
    public String createCustomer(@PathVariable String tourist_email, @PathVariable String tourist_name) {
        System.out.println(tourist_email);
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(tourist_email)
                .setName(tourist_email)
                // add more parameters as needed
                .build();

        try {
            Customer customer = Customer.create(params);
            // Save `customer.getId()` to your database for future payments
            System.out.println(customer.getId());
            return customer.getId();
        } catch (Exception e) {
            // handle error
            return null;
        }
    }

    @GetMapping("/getPaymentMethods/{user_type}/{tourist_email}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethods(@PathVariable String user_type,
                                                                 @PathVariable String tourist_email
    ) throws StripeException {

        List<PaymentMethod> paymentMethods = paymentService.getPaymentMethods(user_type, tourist_email);


        return ResponseEntity.ok(paymentMethods);
    }

    @PostMapping("/addPaymentMethod/{user_type}/{tourist_email}/{payment_method_id}")
    public ResponseEntity<String> addPaymentMethod(@PathVariable String user_type, @PathVariable String tourist_email,
                                                   @PathVariable String payment_method_id) throws StripeException {

        String paymentMethodId = paymentService.addPaymentMethod(user_type, tourist_email,  payment_method_id);


        return ResponseEntity.ok(paymentMethodId);
    }

    @PutMapping("/deletePaymentMethod/{user_type}/{tourist_email}/{payment_method_id}")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable String user_type, @PathVariable String tourist_email,
                                                      @PathVariable String payment_method_id) throws StripeException {

        String paymentMethodId = paymentService.deletePaymentMethod(user_type, tourist_email,  payment_method_id);


        return ResponseEntity.ok(paymentMethodId);
    }

    @GetMapping("/getVendorTotalEarnings/{vendorId}")
    @PreAuthorize("hasRole('VENDOR_ADMIN')")
    public ResponseEntity<BigDecimal> getVendorTotalEarnings(@PathVariable Long vendorId) throws BadRequestException {
        BigDecimal sum = paymentService.getVendorTotalEarnings(vendorId);
        return ResponseEntity.ok(sum);
    }

    @GetMapping("/getTourTotalEarningForLocal/{localId}")
    @PreAuthorize("hasRole('LOCAL')")
    public ResponseEntity<BigDecimal> getTourTotalEarningForLocal(@PathVariable Long localId) {
        BigDecimal sum = paymentService.getTourTotalEarningForLocal(localId);
        return ResponseEntity.ok(sum);
    }

    @PostMapping("/addBankAccount/{userId}/{token}")
    public ResponseEntity<String> addBankAccount(@PathVariable Long userId, @PathVariable String token) throws StripeException, NotFoundException {
        String bankAccountId = paymentService.addBankAccount(userId, token);

        return ResponseEntity.ok(bankAccountId);
    }

    @PutMapping("/deleteBankAccount/{userId}/{bank_account_id}")
    public ResponseEntity<String> deleteBankAccount(@PathVariable Long userId, @PathVariable String bank_account_id) throws StripeException, NotFoundException {
        String deletedBankAccountId = paymentService.deleteBankAccount(userId, bank_account_id);

        return ResponseEntity.ok(deletedBankAccountId);
    }

    @GetMapping("/getBankAccounts/{userId}")
    public ResponseEntity<List<ExternalAccount>> getBankAccounts(@PathVariable Long userId) throws StripeException, NotFoundException {
        List<ExternalAccount> bankAccounts = paymentService.getBankAccounts(userId);
        return ResponseEntity.ok(bankAccounts);
    }

    @PostMapping("/withdrawWallet/{userId}/{bank_account_id}/{amount}")
    public ResponseEntity<String> withdrawWallet(@PathVariable Long userId, @PathVariable String bank_account_id,
                                                 @PathVariable BigDecimal amount) throws StripeException, NotFoundException {
        String payOutId = paymentService.withdrawWallet(userId, bank_account_id, amount);

        return ResponseEntity.ok(payOutId);
    }

    @PostMapping("/topUpWallet/{userId}/{amount}")
    public ResponseEntity<String> topUpWallet(@PathVariable Long userId, @PathVariable BigDecimal amount) throws StripeException, NotFoundException {
        String chargeId = paymentService.topUpWallet(userId, amount);

        return ResponseEntity.ok(chargeId );
    }
}