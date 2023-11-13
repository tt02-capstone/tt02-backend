package com.nus.tt02backend.controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;

import com.nus.tt02backend.services.DataDashboardService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;
import com.stripe.net.Webhook;

@RestController
@CrossOrigin
@RequestMapping("/data")
public class DataDashboardController {

    @Autowired
    DataDashboardService dataDashboardService;

    private final String endpointSecret = "whsec_19210bb5627b1217d600750b798aa67de0d91bdbd5d0ae50efcff3801e3026b6";



    @PostMapping("/bill")
    public ResponseEntity<String> bill(@RequestBody String payload,
                                       @RequestHeader("Stripe-Signature") String sigHeader) throws BadRequestException, IOException, StripeException {



        ObjectMapper objectMapper = new ObjectMapper();
        //objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Assuming the JSON structure is a simple key-value pair at the top level
        Event event = Webhook.constructEvent(
                payload, sigHeader, endpointSecret
        );
        String status = dataDashboardService.bill(event);
        //System.out.println(objectMapper.readValue(request, Invoice.class));

        return ResponseEntity.ok(status);
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

    @PostMapping("/getData/{data_usecase}/{type}/{vendor_id}")
    public ResponseEntity<List<List<Object>>> getData(@PathVariable String data_usecase,
                                                      @PathVariable String type, @PathVariable String vendor_id,
                                                      @RequestBody Map<String, LocalDateTime> dateRangeMap) throws NotFoundException {

        System.out.println(dateRangeMap);
        LocalDateTime start_date = dateRangeMap.get("start_date");
        LocalDateTime end_date = dateRangeMap.get("end_date");

        List<List<Object>> data = dataDashboardService.getData(data_usecase, type, vendor_id, start_date, end_date);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/getPlatformData/{data_usecase}")
    public ResponseEntity<List<List<Object>>> getPlatformData(@PathVariable String data_usecase,
                                                              @RequestBody Map<String, LocalDateTime> dateRangeMap) throws NotFoundException {

        LocalDateTime start_date = dateRangeMap.get("start_date");
        LocalDateTime end_date = dateRangeMap.get("end_date");

        List<List<Object>> data = dataDashboardService.getPlatformData(data_usecase, start_date, end_date);
        return ResponseEntity.ok(data);
    }



}
