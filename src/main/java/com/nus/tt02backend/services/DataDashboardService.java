package com.nus.tt02backend.services;


import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.nus.tt02backend.models.Accommodation;

import com.nus.tt02backend.repositories.*;



import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class DataDashboardService {
    @Autowired
    LocalRepository localRepository;

    @Autowired
    VendorRepository vendorRepository;

    public String getStripe_Id(String user_id, String user_type) throws NotFoundException {
        if (Objects.equals(user_type, "VENDOR")) {
            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(user_id));

            if (vendorOptional.isPresent()) {
                Vendor vendor = vendorOptional.get();

                return vendor.getStripe_account_id();
            } else {
                throw new NotFoundException("Vendor not found");
            }
        } else if (Objects.equals(user_type, "LOCAL")) {
            Optional<Local> localOptional = localRepository.findById(Long.valueOf(user_id));

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                return local.getStripe_account_id();
            } else {
                throw new NotFoundException("Local not found");
            }
        }
        throw new NotFoundException("User Type Not Found");
    }

    public BigDecimal updateWallet(String user_id, String user_type, BigDecimal amount) throws NotFoundException {
        if (Objects.equals(user_type, "VENDOR")) {
            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(user_id));

            if (vendorOptional.isPresent()) {
                Vendor vendor = vendorOptional.get();
                BigDecimal updatedWallet = vendor.getWallet_balance().subtract(amount);
                // To check whether wallet has enough money
                vendor.setWallet_balance(updatedWallet);
                vendorRepository.save(vendor);
                return updatedWallet;

            } else {
                throw new NotFoundException("Vendor not found");
            }
        } else if (Objects.equals(user_type, "LOCAL")) {
            Optional<Local> localOptional = localRepository.findById(Long.valueOf(user_id));

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                BigDecimal updatedWallet = local.getWallet_balance().subtract(amount);
                // To check whether wallet has enough money
                local.setWallet_balance(updatedWallet);
                localRepository.save(local);
                return updatedWallet;
            } else {
                throw new NotFoundException("Local not found");
            }
        }
        throw new NotFoundException("User Type Not Found");
    }

    private final String endpointSecret = "your-webhook-signing-secret-here";
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



    public String createSubscription(String user_id, String user_type, String subscription_type, Boolean auto_renew) throws StripeException, NotFoundException {

        String stripe_account_id = getStripe_Id(user_id, user_type);

        List<Object> items = new ArrayList<>();

        Calendar calendar = new GregorianCalendar();

        Map<String, Object> subscription_params = new HashMap<>();
        Map<String, Object> transaction_params = new HashMap<>();
        subscription_params.put("customer", stripe_account_id);

        Customer customer =
                Customer.retrieve(stripe_account_id);

        Map<String, String> subscription_price = new HashMap<>();
        if (Objects.equals(subscription_type, "MONTHLY")) {

            subscription_price.put(
                    "price",
                    "price_1O1Pf2JuLboRjh4qv1wswh2w"
            );

            calendar.add(Calendar.MONTH, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            subscription_params.put("cancel_at", newCancelAt);

        } else if (Objects.equals(subscription_type, "YEARLY")) {

            subscription_price.put(
                    "price",
                    "price_1O1PfLJuLboRjh4qj2lYrFHi"
            );

            calendar.add(Calendar.YEAR, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            subscription_params.put("cancel_at", newCancelAt);


        }
        items.add(subscription_price );
        subscription_params.put("items", items);
        Price price = Price.retrieve(subscription_price.get("price"));
        BigDecimal subscription_payment = price.getUnitAmountDecimal();
        transaction_params.put("amount",subscription_payment);
        transaction_params.put("currency", "sgd");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transaction_type", "Subscription");
        transaction_params.put("metadata", metadata);

        if (!auto_renew) {
            subscription_params.put("cancel_at_period_end", true);
        }

        Subscription subscription =
                Subscription.create(subscription_params);

        CustomerBalanceTransaction balanceTransaction =
                customer.balanceTransactions().create(transaction_params);

        updateWallet(user_id, user_type, subscription_payment);

        return subscription.getId();
    }

    public String updateSubscription(String subscription_id, String subscription_type, Boolean auto_renew) throws StripeException {

        Subscription subscription =
                Subscription.retrieve(
                        subscription_id
                );

        Calendar calendar = new GregorianCalendar();
        //calendar.setTimeInMillis(currentCancelAt * 1000L);
        Map<String, Object> params = new HashMap<>();
        Map<String, String> subscription_price = new HashMap<>();
        if (Objects.equals(subscription_type, "MONTHLY")) {

            subscription_price.put(
                    "price",
                    "price_1O1Pf2JuLboRjh4qv1wswh2w"
            );

            calendar.add(Calendar.MONTH, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            params.put("cancel_at", newCancelAt);

        } else if (Objects.equals(subscription_type, "YEARLY")) {

            subscription_price.put(
                    "price",
                    "price_1O1PfLJuLboRjh4qj2lYrFHi"
            );

            calendar.add(Calendar.YEAR, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            params.put("cancel_at", newCancelAt);

        }


        List<Object> items = new ArrayList<>();


        if (!auto_renew) {
            params.put("cancel_at_period_end", true);
        }
        items.add(subscription_price );
        params.put("items", items);


        Subscription updatedSubscription =
                subscription.update(params);

        return updatedSubscription.getId();
    }

    // Might need to change

    public String renewSubscription(String subscription_id) throws StripeException, BadRequestException {

        Subscription subscription =
                Subscription.retrieve(
                        subscription_id
                );

        if (Objects.equals(subscription.getStatus(), "active")) {
            Long currentCancelAt = subscription.getCancelAt();
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(currentCancelAt * 1000L); // Stripe timestamps are in seconds
            Price price = subscription.getItems().getData().get(0).getPrice();
            Map<String, Object> params = new HashMap<>();
            if (Objects.equals(price.getId(), "price_1O1Pf2JuLboRjh4qv1wswh2w")) {
                calendar.add(Calendar.MONTH, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                params.put("cancel_at", newCancelAt);
            } else if (Objects.equals(price.getId(), "price_1O1PfLJuLboRjh4qj2lYrFHi")) {
                calendar.add(Calendar.YEAR, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                params.put("cancel_at", newCancelAt);
            }

            Subscription updatedSubscription =
                    subscription.update(params);

            return updatedSubscription.getId();
        } else if (Objects.equals(subscription.getStatus(), "canceled")) {
            Subscription updatedSubscription =
                    subscription.resume();
            return updatedSubscription.getId();
        }


        throw new BadRequestException("Invalid subscription");

    }

    public String cancelSubscription(String subscription_id) throws StripeException {

        Subscription subscription =
                Subscription.retrieve(
                        subscription_id
                );

        Subscription deletedSubscription =
                subscription.cancel();

        return deletedSubscription.getId();
    }

    public String checkSubscriptionStatus() {

        return "";

    }

    public String getSubscriptionStatus(String user_id, String user_type) throws NotFoundException, StripeException {
        String stripe_account_id = getStripe_Id(user_id, user_type);

        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripe_account_id);

        SubscriptionCollection subscriptions =
                Subscription.list(params);

        Subscription current_subscription = subscriptions.getData().get(0);

        return current_subscription.getStatus();
    }

    public Map<String,Object> getSubscription(String user_id, String user_type) throws NotFoundException, StripeException {

        String stripe_account_id = getStripe_Id(user_id, user_type);

        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripe_account_id);

        SubscriptionCollection subscriptions =
                Subscription.list(params);

        Subscription current_subscription = subscriptions.getData().get(0);

        Map<String, Object> extractedFields = new HashMap<>();

        // Convert current_period_end to LocalDate
        Long currentPeriodEndLong = current_subscription.getCurrentPeriodEnd();

        LocalDate currentPeriodEnd = Instant.ofEpochMilli(currentPeriodEndLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // Convert current_period_start to LocalDate
        Long currentPeriodStartLong = current_subscription.getCurrentPeriodStart();

        LocalDate currentPeriodStart = Instant.ofEpochMilli(currentPeriodStartLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();


        // Get recurring interval
        //String recurringInterval = current_subscription.getPlan().getInterval();

        // Get status
        String status = current_subscription.getStatus();

        String subscription_id = current_subscription.getId();

        // Add to the Map
        extractedFields.put("subscription_id",  subscription_id);
        extractedFields.put("current_period_end", currentPeriodEnd);
        extractedFields.put("current_period_start", currentPeriodStart);
        //extractedFields.put("recurring_interval", recurringInterval);
        extractedFields.put("status", status);



        return extractedFields;
    }

    public String getSubscriptions() throws NotFoundException, StripeException {



        Map<String, Object> params = new HashMap<>();


        SubscriptionCollection subscriptions =
                Subscription.list(params);

        Subscription current_subscription = subscriptions.getData().get(0);



        return "";
    }




    UserRepository userRepository;
    @Autowired
    AttractionRepository attractionRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    CartItemRepository cartItemRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    BookingItemRepository bookingItemRepository;
    @Autowired
    CartBookingRepository cartBookingRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    TicketPerDayRepository ticketPerDayRepository;
    @Autowired
    AttractionService attractionService;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    TelecomRepository telecomRepository;
    @Autowired
    RoomRepository roomRepository;
    @Autowired
    TourRepository tourRepository;

    public List<Object[]> getData(String vendorId) throws NotFoundException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();

            List<Accommodation> accommodations = vendor.getAccommodation_list();

            LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

            LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

            List<Object[]> data = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

            return data;
        } else {
            throw new NotFoundException("Vendor not found");
        }

    }

}
