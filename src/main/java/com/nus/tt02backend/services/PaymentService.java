package com.nus.tt02backend.services;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodAttachParams;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    public static void addPaymentMethod(String user_email, String payment_method_id) throws StripeException {


        PaymentMethod paymentMethod =
                PaymentMethod.retrieve(
                        payment_method_id
                );

        Map<String, Object> params = new HashMap<>();
        //To get user based on user_email and get their stripe_account_id
        String stripe_account_id = "";

        params.put("customer", stripe_account_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.attach(params);
    }

}
