package com.nus.tt02backend.controllers;

//import com.nus.tt02backend.services.PaymentService;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/payment")
public class PaymentController {

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
}
