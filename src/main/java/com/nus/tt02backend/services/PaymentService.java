package com.nus.tt02backend.services;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public PaymentIntent createPaymentIntent() throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("usd")
                .setAmount(1000L)  // amount in cents
                .build();
        return PaymentIntent.create(params);
    }
}
