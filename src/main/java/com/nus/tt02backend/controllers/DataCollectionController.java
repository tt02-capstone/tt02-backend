package com.nus.tt02backend.controllers;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RestController
@CrossOrigin
@RequestMapping("/data_collection")
public class DataCollectionController {


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

}
