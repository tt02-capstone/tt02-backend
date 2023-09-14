package com.nus.tt02backend.controllers;

//import com.nus.tt02backend.services.PaymentService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.nus.tt02backend.services.PaymentService;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodListParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



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

    @GetMapping("/getPaymentMethods/{tourist_email}")
    public List<PaymentMethod> getPaymentMethods(@PathVariable String tourist_email) throws StripeException {
        //String stripeCustomerId = get from Tourist based on tourist_email
        System.out.println("Checkmate");
        Map<String, Object> params = new HashMap<>();
        params.put("customer", "cus_OalsHOTNEwycEX");
        params.put("type", "card");
        PaymentMethodCollection paymentMethods =
                PaymentMethod.list(params);

        return paymentMethods.getData();
    }

    @PostMapping("/addPaymentMethod/{tourist_email}/{payment_method_id}")
    public static void addPaymentMethod(@PathVariable String tourist_email, @PathVariable String payment_method_id) throws StripeException {
        //System.out.println(parser);
        PaymentMethod paymentMethod =
                PaymentMethod.retrieve(
                        payment_method_id
                );

        Map<String, Object> params = new HashMap<>();
        //To get user based on user_email and get their stripe_account_id
        String stripe_account_id = "cus_OalsHOTNEwycEX";

        params.put("customer", stripe_account_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.attach(params);
    }
}
