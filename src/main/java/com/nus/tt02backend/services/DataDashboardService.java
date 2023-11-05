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
import java.util.concurrent.TimeUnit;

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

    public LocalDate LongToDate(Long dateLong) {
        return Instant.ofEpochSecond(dateLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public Map<String, Object> getExtractedFields(Subscription subscription) {
        String subscription_id = subscription.getId();
        Boolean auto_renewal = subscription.getCancelAtPeriodEnd();
        String status = subscription.getStatus();
        Map<String, Object> extractedFields = new HashMap<>();
        Long currentPeriodEndLong = subscription.getCurrentPeriodEnd();
        System.out.println(currentPeriodEndLong);
        LocalDate currentPeriodEnd = Instant.ofEpochSecond(currentPeriodEndLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (subscription.getCancelAtPeriodEnd()) {
            extractedFields.put("current_period_start", "NA");




        } else {
            if (subscription.getCancelAt() != null) {
                Long cancelAtLong = subscription.getCancelAt();
                LocalDate cancelAt = Instant.ofEpochSecond(cancelAtLong)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                extractedFields.put("current_period_start", cancelAt);
            }

        }

        if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
            SubscriptionItem subscriptionItem = subscription.getItems().getData().get(0);
            if (subscriptionItem != null) {
                String priceId = subscriptionItem.getPrice().getId(); // Get the price ID
                if (Objects.equals(priceId, "price_1O1Pf2JuLboRjh4qv1wswh2w")) {
                    extractedFields.put("plan",  "Monthly");
                } else if (Objects.equals(priceId, "price_1O1PfLJuLboRjh4qj2lYrFHi")) {
                    extractedFields.put("plan",  "Yearly");
                }
            }
        }

        extractedFields.put("subscription_id",  subscription_id);
        extractedFields.put("current_period_end", currentPeriodEnd);

        extractedFields.put("auto_renewal", auto_renewal);
        extractedFields.put("status", status);

        return extractedFields;
    }



    public String createSubscription(String user_id, String user_type, String subscription_type, Boolean auto_renew) throws StripeException, NotFoundException {

        String stripe_account_id = getStripe_Id(user_id, user_type);

        System.out.println(stripe_account_id);

        List<Object> items = new ArrayList<>();

        Calendar calendar = new GregorianCalendar();

        Map<String, Object> subscription_params = new HashMap<>();
        Map<String, Object> transaction_params = new HashMap<>();
        subscription_params.put("customer", stripe_account_id);

        Customer customer =
                Customer.retrieve(stripe_account_id);

        Map<String, String> subscription_price = new HashMap<>();
        if (Objects.equals(subscription_type, "Monthly")) {

            subscription_price.put(
                    "price",
                    "price_1O1Pf2JuLboRjh4qv1wswh2w"
            );

            calendar.add(Calendar.MONTH, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            subscription_params.put("days_until_due", 30);

        } else if (Objects.equals(subscription_type, "Yearly")) {

            subscription_price.put(
                    "price",
                    "price_1O1PfLJuLboRjh4qj2lYrFHi"
            );

            calendar.add(Calendar.YEAR, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            subscription_params.put("days_until_due", 365);


        }
        items.add(subscription_price);
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

        subscription_params.put("collection_method", "send_invoice");

        Subscription subscription =
                Subscription.create(subscription_params);

        CustomerBalanceTransaction balanceTransaction =
                customer.balanceTransactions().create(transaction_params);

        updateWallet(user_id, user_type, subscription_payment);

        return subscription.getId();
    }

    public Map<String, Object> updateSubscription(String subscription_id, String subscription_type, Boolean auto_renew) throws StripeException {

        Subscription subscription = Subscription.retrieve(subscription_id);

        Map<String, Object> params = new HashMap<>();

        if (!auto_renew) {
            params.put("cancel_at_period_end", true);
        }


        SubscriptionItem subscriptionItem = subscription.getItems().getData().get(0); // Get the first item
        String currentPriceId = subscriptionItem.getPrice().getId(); // Get the current price ID
        String newPriceId = (Objects.equals(subscription_type, "Monthly")) ? "price_1O1Pf2JuLboRjh4qv1wswh2w" : "price_1O1PfLJuLboRjh4qj2lYrFHi";

        if (!currentPriceId.equals(newPriceId)) {
            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("id", subscriptionItem.getId());
            itemParams.put("price", newPriceId); // Set the new price ID

            List<Map<String, Object>> items = new ArrayList<>();
            items.add(itemParams);

            params.put("items", items);
        }

        if (!auto_renew) {
            params.put("cancel_at_period_end", true);
        }

        Subscription updatedSubscription = subscription.update(params);

        Map<String, Object> extractedFields = getExtractedFields(updatedSubscription);

        return extractedFields;
    }

    // Might need to change

    public Map<String,Object> renewSubscription(String subscription_id) throws StripeException, BadRequestException {

        System.out.println(subscription_id);

        Subscription subscription =
                Subscription.retrieve(
                        subscription_id
                );

        if (Objects.equals(subscription.getStatus(), "active")) {
            Long currentCancelAt = subscription.getCancelAt();
            System.out.println(currentCancelAt);
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(currentCancelAt * 1000L); // Stripe timestamps are in seconds
            Price price = subscription.getItems().getData().get(0).getPrice();

            Map<String, Object> params = new HashMap<>();
            params.put("billing_cycle_anchor", "now");
            if (Objects.equals(price.getId(), "price_1O1Pf2JuLboRjh4qv1wswh2w")) {
                calendar.add(Calendar.MONTH, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                long newBillingCycleAnchor = subscription.getCurrentPeriodEnd() + TimeUnit.DAYS.toSeconds(30);
                params.put("billing_cycle_anchor", newBillingCycleAnchor);
                params.put("days_until_due", 30);
            } else if (Objects.equals(price.getId(), "price_1O1PfLJuLboRjh4qj2lYrFHi")) {
                calendar.add(Calendar.YEAR, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                long newBillingCycleAnchor = subscription.getCurrentPeriodEnd() + TimeUnit.DAYS.toSeconds(365);
                params.put("billing_cycle_anchor", newBillingCycleAnchor);
                params.put("days_until_due", 365);
            }

            params.put("cancel_at_period_end", true);



            Subscription updatedSubscription =
                    subscription.update(params);

            System.out.println(updatedSubscription);

            Map<String, Object> extractedFields = getExtractedFields(updatedSubscription);
            return extractedFields;
        } else if (Objects.equals(subscription.getStatus(), "canceled")) {

            Subscription updatedSubscription =
                    subscription.resume();


            Map<String, Object> extractedFields = getExtractedFields(updatedSubscription);
            return extractedFields;
        } else if (Objects.equals(subscription.getStatus(), "incomplete")){

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

            Map<String, Object> extractedFields = getExtractedFields(updatedSubscription);

            return extractedFields;
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

        Map<String, Object> extractedFields = getExtractedFields(current_subscription);



        return extractedFields;
    }

    public List<String> getSubscriptionStatuses(String user_type) throws NotFoundException, StripeException {

        List<Vendor> vendors = vendorRepository.findAll();


        List<String> statuses = new ArrayList<>();

//        Map<String, Object> params = new HashMap<>();
//        params.put("limit", 3);
//
//        SubscriptionCollection subscriptions =
//                Subscription.list(params);

        for (Vendor vendor : vendors) {
            String stripe_id = vendor.getStripe_account_id();
            Map<String, Object> params = new HashMap<>();
            params.put("customer", stripe_id);

            SubscriptionCollection subscriptions =
                    Subscription.list(params);



            if (subscriptions != null) {
                if (subscriptions.getData() != null) {
                    if (!subscriptions.getData().isEmpty()) {
                        String status = subscriptions.getData().get(0).getStatus();

                        statuses.add(status);

                    } else {
                        statuses.add("Never subscribed");
                    }


                }
            }




        }




        return statuses;
    }




    UserRepository userRepository;
    @Autowired
    AttractionRepository attractionRepository;

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
