package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;

import com.nus.tt02backend.services.DataDashboardService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;


import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/data")
public class DataDashboardController {

    @Autowired
    DataDashboardService dataDashboardService;



    private final String endpointSecret = "your-webhook-signing-secret-here";

    @PostMapping("/stripe-webhook")
    public ResponseEntity<String> handleStripeWebhook(HttpServletRequest request) {

        StringBuilder payload = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                payload.append(line);
            }
        } catch (IOException e) {
            return new ResponseEntity<>("Error reading request body", HttpStatus.BAD_REQUEST);
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        Event event = null;

        try {
            event = Webhook.constructEvent(payload.toString(), sigHeader, endpointSecret);
        } catch (StripeException e) {
            return new ResponseEntity<>("Invalid signature", HttpStatus.BAD_REQUEST);
        }

        // Handle the event
        if ("invoice.created".equals(event.getType())) {
            // Your logic here
        }

        return new ResponseEntity<>("Received", HttpStatus.OK);
    }

    @GetMapping("/getSubscriptionStatus/{user_id}/{user_type}")
    public ResponseEntity<String> getSubscriptionStatus(@PathVariable String user_id, @PathVariable String user_type) throws StripeException, NotFoundException {
        String subscriptionStatus = dataDashboardService.getSubscriptionStatus(user_id,user_type);

        return ResponseEntity.ok(subscriptionStatus);
    }

    @GetMapping("/getSubscription/{user_id}/{user_type}")
    public ResponseEntity<Map<String,Object>> getSubscription(@PathVariable String user_id, @PathVariable String user_type) throws StripeException, NotFoundException {
        Map<String,Object> subscriptionDetails = dataDashboardService.getSubscription(user_id,user_type);

        return ResponseEntity.ok(subscriptionDetails);
    }

    @GetMapping("/getSubscriptionStatuses/{user_type}")
    public ResponseEntity<List<String>> getSubscriptionStatuses( @PathVariable String user_type) throws StripeException, NotFoundException {
        List<String> subscriptionDetails = dataDashboardService.getSubscriptionStatuses(user_type);

        return ResponseEntity.ok(subscriptionDetails);
    }

    @PostMapping("/subscribe/{user_id}/{user_type}/{subscription_type}/{auto_renew}")
    public ResponseEntity<String> subscribe(@PathVariable String user_id, @PathVariable String user_type,
                                            @PathVariable String subscription_type, @PathVariable Boolean auto_renew) throws StripeException, NotFoundException {
        String subscriptionStatus = dataDashboardService.createSubscription(user_id,user_type, subscription_type, auto_renew);

        return ResponseEntity.ok(subscriptionStatus);
    }

    @PutMapping("/updateSubscription/{subscription_id}/{subscription_type}/{auto_renew}")
    public ResponseEntity<Map<String,Object>> updateSubscription(@PathVariable String subscription_id,
                                                    @PathVariable String subscription_type, @PathVariable Boolean auto_renew) throws StripeException, NotFoundException {
        Map<String,Object> subscriptionStatus = dataDashboardService.updateSubscription(subscription_id, subscription_type, auto_renew);

        return ResponseEntity.ok(subscriptionStatus);
    }

    @PutMapping("/renewSubscription/{subscription_id}")
    public ResponseEntity<Map<String,Object>> renewSubscription(@PathVariable String subscription_id) throws StripeException, NotFoundException, BadRequestException {
        Map<String,Object> subscriptionStatus = dataDashboardService.renewSubscription(subscription_id);

        return ResponseEntity.ok(subscriptionStatus);
    }

    @DeleteMapping("/cancelSubscription/{subscription_id}")
    public ResponseEntity<String> cancelSubscription(@PathVariable String subscription_id) throws StripeException, NotFoundException {
        String subscriptionStatus = dataDashboardService.cancelSubscription(subscription_id);

        return ResponseEntity.ok(subscriptionStatus);
    }

    @GetMapping("/getData/{data_usecase}/{type}/{vendor_id}")
    public ResponseEntity<List<List<Object>>> getData(@PathVariable String data_usecase, @PathVariable String type, @PathVariable String vendor_id) throws NotFoundException {

        List<List<Object>> data = dataDashboardService.getData(data_usecase, type, vendor_id);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/getPlatformData/{data_usecase}")
    public ResponseEntity<List<List<Object>>> getPlatformData(@PathVariable String data_usecase) throws NotFoundException {

        List<List<Object>> data = dataDashboardService.getPlatformData(data_usecase);
        return ResponseEntity.ok(data);
    }



}
