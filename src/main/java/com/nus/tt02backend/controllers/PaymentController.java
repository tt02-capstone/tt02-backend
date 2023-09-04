package com.nus.tt02backend.controllers;

import com.nus.tt02backend.services.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/tourist/create-payment-intent")
    public Map<String, String> createPaymentIntent() throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(1000L) // amount in cents
                .build();
        System.out.println("checkmate");
        // Create a PaymentIntent
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        System.out.println("wat");
        String clientSecret = paymentIntent.getClientSecret();

        // Prepare response
        Map<String, String> responseData = new HashMap<>();
        responseData.put("clientSecret", clientSecret);
        System.out.println(responseData);
        return responseData;
    }
}
