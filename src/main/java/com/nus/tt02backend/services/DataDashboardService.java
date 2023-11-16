package com.nus.tt02backend.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import com.nus.tt02backend.repositories.*;



import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DataDashboardService {
    @Autowired
    LocalRepository localRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    JavaMailSender javaMailSender;


    private final String endpointSecret = "whsec_19210bb5627b1217d600750b798aa67de0d91bdbd5d0ae50efcff3801e3026b6";

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

    public String bill(Event payload) throws BadRequestException, StripeException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Vendor vendor = null;

        String eventType = payload.getType();

        System.out.println("Event Type: " + eventType);

        if (eventType.equals("invoice.created")) {// Your code to handle the invoice.upcoming event
            System.out.println("Received created invoice");
            //System.out.println(payload);
            EventDataObjectDeserializer dataObjectDeserializer =  payload.getDataObjectDeserializer();
            System.out.println(payload.getData());
            System.out.println(dataObjectDeserializer.getRawJson());
            System.out.println(dataObjectDeserializer.getObject().isPresent());
            if (dataObjectDeserializer.getObject().isPresent()) {
                Invoice invoice = (Invoice) dataObjectDeserializer.getObject().get();
                System.out.println(invoice.getId());
                Map<String, Object> params = new HashMap<>();
                vendor = vendorRepository.findByStripeId(invoice.getCustomer());
                BigDecimal payment_amount = BigDecimal.valueOf(invoice.getAmountDue()).divide(BigDecimal.valueOf(100));
                BigDecimal currentWalletBalance  = vendor.getWallet_balance();
                if (currentWalletBalance.compareTo(payment_amount) >= 0) {
                    vendor.setWallet_balance(currentWalletBalance.subtract(payment_amount));

                } else {
                    BigDecimal remainder = payment_amount.subtract(currentWalletBalance);
                    if (currentWalletBalance.compareTo(BigDecimal.ZERO) > 0) {
                        vendor.setWallet_balance(BigDecimal.ZERO);
                    }

                    Map<String, Object> automaticPaymentMethods = new HashMap<>();
                    automaticPaymentMethods.put("enabled", true);

                    Map<String, Object> paymentParams = new HashMap<>();
                    paymentParams.put("amount", remainder.multiply(new BigDecimal("100")).intValueExact());
                    paymentParams.put("currency", "usd");
                    paymentParams.put("confirm", true);


                    paymentParams.put("customer", invoice.getCustomer());
                    paymentParams.put("return_url", "yourappname://stripe/callback");

                    PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);



                }

                vendorRepository.save(vendor);
                params.put("paid_out_of_band", true);
                System.out.println("Finalizing invoice...");
                Invoice invoiceToPay = Invoice.retrieve(invoice.getId());
                Invoice finalizedInvoice = invoiceToPay.finalizeInvoice();

                System.out.println(finalizedInvoice);
                System.out.println("Paying invoice...");
                finalizedInvoice.pay(params);
                System.out.println("Success");

            }




        } else {

            throw new BadRequestException("Unhandled event type");
        }

            // Return a 200 response to acknowledge receipt of the event
        return "Received.";


//        try (BufferedReader reader = request.getReader()) {
//            // Read the request body to get the payload
//            StringBuilder payload = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                payload.append(line);
//            }
//
//            // Retrieve the signature from the headers
//            String sigHeader = request.getHeader("Stripe-Signature");
//
//            // Verify and construct the event
//            Event event = null;
//            try {
//                event = Webhook.constructEvent(
//                        payload.toString(),
//                        sigHeader,
//                        endpointSecret
//                );
//            } catch (SignatureVerificationException e) {
//                System.out.println("⚠️  Webhook error while validating signature.");
//                throw new BadRequestException("Invalid signature.");
//
//            }
//
//
//            // Handle the event
//            if (event.getType().equals("invoice.created")) {// Your code to handle the invoice.upcoming event
//                System.out.println("Received");
//
//                Invoice invoice = (Invoice) event.getData().getObject();
//                if ("open".equals(invoice.getStatus())) {
//                    // If the payment for the invoice is handled internally,
//                    // mark the invoice as paid out of band
//                    Map<String, Object> params = new HashMap<>();
//                    vendor = vendorRepository.findByStripeId(invoice.getCustomer());
//                    BigDecimal payment_amount = BigDecimal.valueOf(invoice.getAmountDue()).divide(BigDecimal.valueOf(100));
//                    BigDecimal currentWalletBalance  = vendor.getWallet_balance();
//                    if (currentWalletBalance.compareTo(payment_amount) >= 0) {
//                        vendor.setWallet_balance(currentWalletBalance.subtract(payment_amount));
//
//                    } else {
//                        BigDecimal remainder = payment_amount.subtract(currentWalletBalance);
//                        if (currentWalletBalance.compareTo(BigDecimal.ZERO) >0 ) {
//                            vendor.setWallet_balance(BigDecimal.ZERO);
//
//                        }
//
//                        Map<String, Object> automaticPaymentMethods = new HashMap<>();
//                        automaticPaymentMethods.put("enabled", true);
//
//                        Map<String, Object> paymentParams = new HashMap<>();
//                        paymentParams.put("amount", remainder.multiply(new BigDecimal("100")).intValueExact());
//                        paymentParams.put("currency", "usd");
//                        paymentParams.put("confirm", true);
//
//
//                        paymentParams.put("customer", invoice.getCustomer());
//                        paymentParams.put("return_url", "yourappname://stripe/callback");
//
//                        PaymentIntent paymentIntent = PaymentIntent.create(paymentParams);
//
//
//                    }
//                    vendorRepository.save(vendor);
//                    params.put("paid_out_of_band", true);
//                    invoice = invoice.pay(params);
//                }
//
//            } else {
//
//                throw new BadRequestException("Unhandled event type");
//            }
//
//            // Return a 200 response to acknowledge receipt of the event
//            return "Received.";
//
//        } catch (IOException e) {
//            throw new BadRequestException("An error occurred while processing the request.");
//
//        } catch (StripeException e) {
//            String subject = "[WithinSG] Error Processing Subscription Payment";
//            String content = "<p>Dear " + vendor.getPoc_name() + ",</p>" +
//                    "<p>Thank you for registering for a vendor account with WithinSG. " +
//                    "We are glad that you have chosen us as your service provider!</p>" +
//
//                    "<p>We have received your application and it is in the midst of processing. " +
//                    "Please verify your email address by clicking on the button below.</p>" +
//                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
//                    "Verify Email</button></a>" +
//                    "<p>An email will be sent to you once your account has been activated.</p>" +
//                    "<p>Kind Regards,<br> WithinSG</p>";
//
//            try {
//                List<VendorStaff> staffs = vendor.getVendor_staff_list();
//                String email = staffs.get(0).getEmail();
//                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
//                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
//                mimeMessageHelper.setTo(email);
//                mimeMessageHelper.setSubject(subject);
//                mimeMessageHelper.setText(content, true);
//                javaMailSender.send(mimeMessage);
//            } catch (MessagingException r) {
//                throw new BadRequestException("An error occurred while processing the payment.");
//            }


            //return "Received.";

        //}
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




    public LocalDate LongToDate(Long dateLong) {
        return Instant.ofEpochSecond(dateLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public Map<String, Object> getExtractedFields(Subscription subscription) {
        System.out.println(subscription);
        String subscription_id = subscription.getId();
        Boolean auto_renewal = Boolean.valueOf(subscription.getMetadata().get("auto_renewal"));
        String status = subscription.getStatus();
        Map<String, Object> extractedFields = new HashMap<>();
        Long currentPeriodEndLong = subscription.getCurrentPeriodEnd();
        System.out.println(currentPeriodEndLong);
        LocalDate currentPeriodEnd = Instant.ofEpochSecond(currentPeriodEndLong)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (auto_renewal) {
            extractedFields.put("current_period_end", "-");




        } else {
            if (subscription.getCancelAt() != null) {
                Long cancelAtLong = subscription.getCancelAt();
                LocalDate cancelAt = Instant.ofEpochSecond(cancelAtLong)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                extractedFields.put("current_period_end", cancelAt);
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
        if (!auto_renewal && subscription.getCancelAtPeriodEnd()) {

            extractedFields.put("current_period_start", "-");
        } else {
            extractedFields.put("current_period_start", currentPeriodEnd);
        }





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
        long newCancelAt = calendar.getTimeInMillis();
        Map<String, String> subscription_price = new HashMap<>();
        if (Objects.equals(subscription_type, "Monthly")) {

            subscription_price.put(
                    "price",
                    "price_1O1Pf2JuLboRjh4qv1wswh2w"
            );

            calendar.add(Calendar.MONTH, 1);
            newCancelAt = calendar.getTimeInMillis() / 1000L;
            subscription_params.put("days_until_due", 30);

        } else if (Objects.equals(subscription_type, "Yearly")) {

            subscription_price.put(
                    "price",
                    "price_1O1PfLJuLboRjh4qj2lYrFHi"
            );

            calendar.add(Calendar.YEAR, 1);
            newCancelAt = calendar.getTimeInMillis() / 1000L;
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

        Map<String, Object> sub_metadata = new HashMap<>();

        if (!auto_renew) {
            subscription_params.put("cancel_at_period_end", true);
            sub_metadata.put("auto_renewal", false);


        } else {
            subscription_params.put("cancel_at", newCancelAt);
            sub_metadata.put("auto_renewal", true);
        }

        subscription_params.put("metadata", sub_metadata);

        subscription_params.put("collection_method", "send_invoice");

        Subscription subscription =
                Subscription.create(subscription_params);

        CustomerBalanceTransaction balanceTransaction =
                customer.balanceTransactions().create(transaction_params);

        //updateWallet(user_id, user_type, subscription_payment);

        return subscription.getId();
    }

    public Map<String, Object> updateSubscription(String subscription_id, String subscription_type, Boolean auto_renew) throws StripeException {

        Subscription subscription = Subscription.retrieve(subscription_id);

        Map<String, Object> params = new HashMap<>();


        SubscriptionItem subscriptionItem = subscription.getItems().getData().get(0); // Get the first item
        String currentPriceId = subscriptionItem.getPrice().getId(); // Get the current price ID
        String newPriceId = (Objects.equals(subscription_type, "Monthly")) ? "price_1O1Pf2JuLboRjh4qv1wswh2w" : "price_1O1PfLJuLboRjh4qj2lYrFHi";

        if (!currentPriceId.equals(newPriceId)) {
            System.out.println("Change of plans");
            Map<String, Object> itemParams = new HashMap<>();
            itemParams.put("id", subscriptionItem.getId());
            itemParams.put("price", newPriceId); // Set the new price ID

            List<Map<String, Object>> items = new ArrayList<>();
            items.add(itemParams);

            params.put("items", items);
        }

        Map<String, Object> sub_metadata = new HashMap<>();

        if (!auto_renew) {
            params.put("cancel_at_period_end", true);
            sub_metadata.put("auto_renewal", false);
        } else {
            sub_metadata.put("auto_renewal", true);
        }

        params.put("metadata", sub_metadata);

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
            //params.put("billing_cycle_anchor", "unchanged");
            if (Objects.equals(price.getId(), "price_1O1Pf2JuLboRjh4qv1wswh2w")) {
                calendar.add(Calendar.MONTH, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                long newBillingCycleAnchor = subscription.getCurrentPeriodEnd() + TimeUnit.DAYS.toSeconds(30);
                //params.put("billing_cycle_anchor", newBillingCycleAnchor);
                params.put("days_until_due", 30);
                params.put("cancel_at", newCancelAt);
            } else if (Objects.equals(price.getId(), "price_1O1PfLJuLboRjh4qj2lYrFHi")) {
                calendar.add(Calendar.YEAR, 1);
                long newCancelAt = calendar.getTimeInMillis() / 1000L;
                long newBillingCycleAnchor = subscription.getCurrentPeriodEnd() + TimeUnit.DAYS.toSeconds(365);
                //params.put("billing_cycle_anchor", newBillingCycleAnchor);
                params.put("days_until_due", 365);
                params.put("cancel_at", newCancelAt);
            }

           //params.put("cancel_at_period_end", true);



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

    public String removeSubscription(String subscription_id) throws StripeException {

        Subscription subscription =
                Subscription.retrieve(
                        subscription_id
                );

        Subscription deletedSubscription =
                subscription.cancel();

        return deletedSubscription.getId();
    }

    public Map<String,Object> cancelSubscription(String subscription_id) throws StripeException {

        Subscription subscription = Subscription.retrieve(subscription_id);

        //Boolean updatedAutoRenew = !(Boolean.valueOf(subscription.getMetadata().get("auto_renewal")));

        Map<String, Object> params = new HashMap<>();

        params.put("cancel_at_period_end", true);
        Long currentCancelAt = subscription.getCancelAt();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(currentCancelAt * 1000L); // Stripe timestamps are in seconds
        Price price = subscription.getItems().getData().get(0).getPrice();

        if (Objects.equals(price.getId(), "price_1O1Pf2JuLboRjh4qv1wswh2w")) {
            calendar.add(Calendar.MONTH, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            //params.put("cancel_at", newCancelAt);

        } else if (Objects.equals(price.getId(), "price_1O1PfLJuLboRjh4qj2lYrFHi")) {
            calendar.add(Calendar.YEAR, 1);
            long newCancelAt = calendar.getTimeInMillis() / 1000L;
            //params.put("cancel_at", newCancelAt);

        }

        Map<String, Object> sub_metadata = new HashMap<>();

        //params.put("cancel_at_period_end", true);
        sub_metadata.put("auto_renewal", false);

        params.put("metadata", sub_metadata);

        Subscription updatedSubscription = subscription.update(params);

        Map<String, Object> extractedFields = getExtractedFields(updatedSubscription);

        return extractedFields;


    }


    public String getSubscriptionStatus(String user_id, String user_type) throws NotFoundException, StripeException {
        String stripe_account_id = getStripe_Id(user_id, user_type);

        Map<String, Object> params = new HashMap<>();
        params.put("customer", stripe_account_id);

        SubscriptionCollection subscriptions =
                Subscription.list(params);

        String status = "";

        if (subscriptions.getData().size() > 0) {
            Subscription current_subscription = subscriptions.getData().get(0);

            return current_subscription.getStatus();
        } else {
            return "Never subscribed";
        }


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
    AccommodationRepository accommodationRepository;

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

    Map<String, String> countryCodeToCountry = new HashMap<String, String>() {
        {
            put("+62", "Indonesia");
            put("+86", "China");
            put("+60", "Malaysia");
            put("+61", "Australia");
            put("+91", "India");
            put("+63", "Philippines");
            put("+1", "United States");
            put("+82", "South Korea");
            put("+84", "Vietnam");
            put("+44", "United Kingdom");
            put("+65", "Singapore");
        }
    };

    public List<List<Object>> getData(String data_usecase, String type, String vendorId, LocalDateTime start_date,
                                      LocalDateTime end_date) throws NotFoundException {


        if (Objects.equals(data_usecase, "Total Bookings Over Time")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

                Map<LocalDate, Integer> dateCounts = new HashMap<>();

                System.out.println(bookings.size());

                List<List<Object>> dateCountryList = new ArrayList<>();

                for (Booking booking : bookings) {
                    LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                    String countryCode = null;

                    // Determine which user type (tourist or local) is not null and get the country code
                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                    // Create [Date, Country] pair and add to the list
                    List<Object> dateCountryPair = Arrays.asList(bookingDate, country);
                    dateCountryList.add(dateCountryPair);
                }

                System.out.println(dateCountryList);

                return dateCountryList;
            } else {
                throw new NotFoundException("Vendor not found");
            }

        } else if (Objects.equals(data_usecase, "Revenue Over Time")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

                Map<LocalDate, Integer> dateCounts = new HashMap<>();
                List<List<Object>> dateCountryRevenueList = new ArrayList<>();

                for (Booking booking : bookings) {
                    LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                    BigDecimal commission = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));
                    BigDecimal revenue = booking.getPayment().getPayment_amount().subtract(commission);
                    String countryCode = null;

                    // Determine which user type (tourist or local) is not null and get the country code
                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                    // Create [Date, Country, Revenue] triple and add to the list
                    List<Object> dateCountryRevenueTriple = Arrays.asList(bookingDate, country, revenue);
                    dateCountryRevenueList.add(dateCountryRevenueTriple);
                }

                System.out.println(dateCountryRevenueList);

                return dateCountryRevenueList;

            } else {
                throw new NotFoundException("Vendor not found");
            }

        } else if (Objects.equals(data_usecase, "Revenue to Bookings Ratio Over Time")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

                Map<LocalDate, Integer> dateCounts = new HashMap<>();
                List<List<Object>> dateCountryRevenueList = new ArrayList<>();

                for (Booking booking : bookings) {
                    LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                    BigDecimal commission = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));
                    BigDecimal revenue = booking.getPayment().getPayment_amount().subtract(commission);
                    String countryCode = null;

                    // Determine which user type (tourist or local) is not null and get the country code
                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                    // Create [Date, Country, Revenue] triple and add to the list
                    List<Object> dateCountryRevenueTriple = Arrays.asList(bookingDate, country, revenue);
                    dateCountryRevenueList.add(dateCountryRevenueTriple);
                }

                System.out.println(dateCountryRevenueList);

                return dateCountryRevenueList;

            } else {
                throw new NotFoundException("Vendor not found");
            }

        }
        else if (Objects.equals(data_usecase, "Bookings Breakdown")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

                Map<LocalDate, Integer> dateCounts = new HashMap<>();
                List<List<Object>> dateCountryRevenueList = new ArrayList<>();

                Map<String, Object> result = new HashMap<>();
                Map<String, Integer> categoryCounts = new HashMap<>();
                Map<String, Integer> countryCounts = new HashMap<>();
                Map<String, Integer> statusCounts = new HashMap<>();

                for (Booking booking : bookings) {

                    String countryCode = null;

                    String category = "";

                    if (Objects.equals(type, "ACCOMMODATION")) {
                        category = String.valueOf(booking.getRoom().getRoom_type());
                    } else if (Objects.equals(type, "ATTRACTION")) {
                        category = booking.getActivity_name();
                    } else if (Objects.equals(type, "TELECOM")) {
                        category = booking.getActivity_name();
                    } else if (Objects.equals(type, "TOUR")) {
                        category = booking.getActivity_name();
                    }

                    //String roomType = String.valueOf(booking.getRoom().getRoom_type());
                    categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);

                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown");

                    countryCounts.put(country, countryCounts.getOrDefault(country, 0) + 1);

                    String bookingStatus = String.valueOf(booking.getStatus());

                    statusCounts.put(bookingStatus, statusCounts.getOrDefault(bookingStatus, 0) + 1);


                }

                result.put("Category", categoryCounts);
                result.put("Country", countryCounts);
                result.put("Status", statusCounts);

                System.out.println(result);

                List<Object> placeholderList = new ArrayList<>();

                placeholderList.add(result);

                dateCountryRevenueList.add(placeholderList);


                System.out.println(dateCountryRevenueList);

                return dateCountryRevenueList;


            } else {
                throw new NotFoundException("Vendor not found");
            }

        } else if (Objects.equals(data_usecase, "Revenue Breakdown")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {

                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();


                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

                Map<LocalDate, Integer> dateCounts = new HashMap<>();
                List<List<Object>> dateCountryRevenueList = new ArrayList<>();

                Map<String, Object> result = new HashMap<>();
                Map<String, BigDecimal> categoryRevenues = new HashMap<>();
                Map<String, BigDecimal> countryRevenues = new HashMap<>();
                Map<String, BigDecimal> statusRevenues = new HashMap<>();

                for (Booking booking : bookings) {

                    BigDecimal commission = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));
                    BigDecimal revenue = booking.getPayment().getPayment_amount().subtract(commission);

                    String countryCode = null;

                    String category = "";

                    if (Objects.equals(type, "ACCOMMODATION")) {
                        category = String.valueOf(booking.getRoom().getRoom_type());
                    } else if (Objects.equals(type, "ATTRACTION")) {
                        category = booking.getActivity_name();
                    } else if (Objects.equals(type, "TELECOM")) {
                        category = booking.getActivity_name();
                    } else if (Objects.equals(type, "TOUR")) {
                        category = booking.getActivity_name();
                    }


                    //String roomType = String.valueOf(booking.getRoom().getRoom_type());
                    categoryRevenues.put(category, categoryRevenues.getOrDefault(category, BigDecimal.valueOf(0)).add(revenue));

                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown");

                    countryRevenues.put(country, countryRevenues.getOrDefault(country,  BigDecimal.valueOf(0)).add(revenue));

                    String bookingStatus = String.valueOf(booking.getStatus());

                    statusRevenues.put(bookingStatus, statusRevenues.getOrDefault(bookingStatus,  BigDecimal.valueOf(0)).add(revenue));


                }

                result.put("Category", categoryRevenues);
                result.put("Country", countryRevenues);
                result.put("Status", statusRevenues);

                System.out.println(result);

                List<Object> placeholderList = new ArrayList<>();

                placeholderList.add(result);

                dateCountryRevenueList.add(placeholderList);


                System.out.println(dateCountryRevenueList);

                return dateCountryRevenueList;

            } else {
                throw new NotFoundException("Vendor not found");
            }


        } else if (Objects.equals(data_usecase, "Customer Retention Over Time")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(start_date, end_date, 1L, type);

// Create a map to store the booking count, revenue, and country for each date
                Map<LocalDate, List<Object>> bookingDataByDate = new HashMap<>();

                for (Booking booking : bookings) {
                    LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                    BigDecimal revenue = booking.getPayment().getPayment_amount();
                    String countryCode = null;

                    // Determine which user type (tourist or local) is not null and get the country code
                    if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                        countryCode = booking.getTourist_user().getCountry_code();
                    } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                        countryCode = booking.getLocal_user().getCountry_code();
                    }

                    String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                    // Check if the booking date is already in the map
                    if (bookingDataByDate.containsKey(bookingDate)) {
                        List<Object> existingData = bookingDataByDate.get(bookingDate);
                        int currentCount = (int) existingData.get(0);
                        BigDecimal currentRevenue = (BigDecimal) existingData.get(1);

                        // Update the count and revenue
                        existingData.set(0, currentCount + 1);
                        existingData.set(1, currentRevenue.add(revenue));
                    } else {
                        // Initialize the list with count, revenue, and country
                        List<Object> newData = new ArrayList<>();
                        newData.add(1); // Count for the first booking on this date
                        newData.add(revenue);
                        newData.add(country);
                        bookingDataByDate.put(bookingDate, newData);
                    }
                }

// Convert the map to a list of [Date, Count, Revenue, Country] for sending to the frontend
                List<List<Object>> dateBookingDataList = new ArrayList<>();
                for (Map.Entry<LocalDate, List<Object>> entry : bookingDataByDate.entrySet()) {
                    LocalDate date = entry.getKey();
                    List<Object> data = entry.getValue();
                    dateBookingDataList.add(Arrays.asList(date, data.get(0), data.get(1), data.get(2)));
                }

                System.out.println(dateBookingDataList);

                return dateBookingDataList;



            } else {
                throw new NotFoundException("Vendor not found");
            }

        }

        throw new NotFoundException("Data Use Case Not Found");



    }


    public List<List<Object>> getPlatformData(String data_usecase, LocalDateTime start_date,
                                              LocalDateTime end_date) throws NotFoundException {

        LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

        LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

        List<Booking> bookings = bookingRepository.getPlatformBookingsOverTime(start_date, end_date);

        if (Objects.equals(data_usecase, "Platform Bookings Over Time")) {

            Map<LocalDate, Integer> dateCounts = new HashMap<>();

            List<List<Object>> dateCountryList = new ArrayList<>();

            for (Booking booking : bookings) {
                LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                String countryCode = null;

                // Determine which user type (tourist or local) is not null and get the country code
                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                String category = String.valueOf(booking.getType());

                //String status = String.valueOf(booking.getStatus());

                // Sub-category

                String subcategory = "";

                String vendorName = "";

                if (booking.getAttraction() != null) {

                    subcategory = String.valueOf(booking.getAttraction().getAttraction_category());

                    vendorName = vendorRepository.findVendorByAttractionName(booking.getAttraction().getName()).getBusiness_name();

                } else if (booking.getRoom() != null) {

                    //String accommodation = accommodationRepository.getAccomodationByRoomId(booking.getRoom().getRoom_id());

                    subcategory = String.valueOf(booking.getRoom().getRoom_type()); // To change to accoms

                    Vendor vendor = vendorRepository.findVendorByRoomId(booking.getRoom().getRoom_id());


                    vendorName = vendor.getBusiness_name();

                } else if (booking.getTour() != null) {

                    subcategory = "Tour"; // Get tour's attraction

                    vendorName = localRepository.findLocalByTour(booking.getTour()).getName();


                } else if (booking.getTelecom() != null) {

                    subcategory = booking.getTelecom().getName();

                    vendorName = vendorRepository.findVendorByTelecomName(booking.getTelecom().getName()).getBusiness_name();

                } else if (booking.getItem() != null) {

                    subcategory = "Item";

                    vendorName = vendorRepository.findVendorByItemId(booking.getItem().getItem_id()).getBusiness_name();

                }



                // Vendor


                // Create [Date, Country] pair and add to the list
                List<Object> dateCountryPair = Arrays.asList(bookingDate, country, category, subcategory, vendorName);
                dateCountryList.add(dateCountryPair);
            }

            System.out.println(dateCountryList);

            return dateCountryList;

        } else if (Objects.equals(data_usecase, "Platform Revenue Over Time")) {

            Map<LocalDate, Integer> dateCounts = new HashMap<>();
            List<List<Object>> dateCountryRevenueList = new ArrayList<>();

            for (Booking booking : bookings) {
                LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                BigDecimal revenue = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));

                String countryCode = null;

                // Determine which user type (tourist or local) is not null and get the country code
                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                String category = String.valueOf(booking.getType());

                String subcategory = "";

                String vendorName = "";

                if (booking.getAttraction() != null) {

                    subcategory = String.valueOf(booking.getAttraction().getAttraction_category());

                    vendorName = vendorRepository.findVendorByAttractionName(booking.getAttraction().getName()).getBusiness_name();

                } else if (booking.getRoom() != null) {

                    //String accommodation = accommodationRepository.getAccomodationByRoomId(booking.getRoom().getRoom_id());

                    subcategory = String.valueOf(booking.getRoom().getRoom_type()); // To change to accoms

                    Vendor vendor = vendorRepository.findVendorByRoomId(booking.getRoom().getRoom_id());


                    vendorName = vendor.getBusiness_name();

                } else if (booking.getTour() != null) {

                    subcategory = "Tour"; // Get tour's attraction

                    vendorName = localRepository.findLocalByTour(booking.getTour()).getName();


                } else if (booking.getTelecom() != null) {

                    subcategory = booking.getTelecom().getName();

                    vendorName = vendorRepository.findVendorByTelecomName(booking.getTelecom().getName()).getBusiness_name();

                } else if (booking.getItem() != null) {

                    subcategory = "Item";

                    vendorName = vendorRepository.findVendorByItemId(booking.getItem().getItem_id()).getBusiness_name();

                }

                // Create [Date, Country, Revenue] triple and add to the list
                List<Object> dateCountryRevenueTriple = Arrays.asList(bookingDate, country, revenue, category, subcategory, vendorName);
                dateCountryRevenueList.add(dateCountryRevenueTriple);

                // To add vendors
            }

            System.out.println(dateCountryRevenueList);

            return dateCountryRevenueList;

        } else if (Objects.equals(data_usecase, "Platform Bookings Breakdown")) {

            Map<LocalDate, Integer> dateCounts = new HashMap<>();
            List<List<Object>> dateCountryRevenueList = new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            Map<String, Integer> categoryCounts = new HashMap<>();
            Map<String, Integer> subcategoryCounts = new HashMap<>();
            Map<String, Integer> vendorCounts = new HashMap<>();
            Map<String, Integer> countryCounts = new HashMap<>();
            Map<String, Integer> statusCounts = new HashMap<>();

            for (Booking booking : bookings) {

                String countryCode = null;
                String category = String.valueOf(booking.getType());


                categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);

                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown");

                countryCounts.put(country, countryCounts.getOrDefault(country, 0) + 1);

                String subcategory = "";

                String vendorName = "";

                if (booking.getAttraction() != null) {

                    subcategory = String.valueOf(booking.getAttraction().getAttraction_category());

                    vendorName = vendorRepository.findVendorByAttractionName(booking.getAttraction().getName()).getBusiness_name();

                } else if (booking.getRoom() != null) {

                    //String accommodation = accommodationRepository.getAccomodationByRoomId(booking.getRoom().getRoom_id());

                    subcategory = String.valueOf(booking.getRoom().getRoom_type()); // To change to accoms

                    Vendor vendor = vendorRepository.findVendorByRoomId(booking.getRoom().getRoom_id());


                    vendorName = vendor.getBusiness_name();

                } else if (booking.getTour() != null) {

                    subcategory = "Tour"; // Get tour's attraction

                    vendorName = localRepository.findLocalByTour(booking.getTour()).getName();


                } else if (booking.getTelecom() != null) {

                    subcategory = booking.getTelecom().getName();

                    vendorName = vendorRepository.findVendorByTelecomName(booking.getTelecom().getName()).getBusiness_name();

                } else if (booking.getItem() != null) {

                    subcategory = "Item";

                    vendorName = vendorRepository.findVendorByItemId(booking.getItem().getItem_id()).getBusiness_name();

                }

                subcategoryCounts.put(subcategory, subcategoryCounts.getOrDefault(subcategory, 0) + 1);

                vendorCounts.put(vendorName,  vendorCounts.getOrDefault(vendorName, 0) + 1);

                String bookingStatus = String.valueOf(booking.getStatus());

                statusCounts.put(bookingStatus, statusCounts.getOrDefault(bookingStatus, 0) + 1);


            }

            result.put("Category", categoryCounts);
            result.put("Country", countryCounts);
            result.put("Status", statusCounts);
            result.put("Vendor", vendorCounts);
            result.put("Subcategory", subcategoryCounts);

            System.out.println(result);

            List<Object> placeholderList = new ArrayList<>();

            placeholderList.add(result);

            dateCountryRevenueList.add(placeholderList);

            return dateCountryRevenueList;

        } else if (Objects.equals(data_usecase, "Platform Revenue Breakdown")) {

            Map<String, Object> result = new HashMap<>();
            List<List<Object>> dateCountryRevenueList = new ArrayList<>();
            Map<String, BigDecimal> categoryRevenues = new HashMap<>();
            Map<String, BigDecimal> countryRevenues = new HashMap<>();
            Map<String, BigDecimal> statusRevenues = new HashMap<>();
            Map<String, BigDecimal> subcategoryRevenues = new HashMap<>();
            Map<String, BigDecimal> vendorRevenues = new HashMap<>();

            for (Booking booking : bookings) {

                BigDecimal revenue = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));

                String countryCode = null;
                String category = String.valueOf(booking.getType());

                categoryRevenues.put(category, categoryRevenues.getOrDefault(category, BigDecimal.valueOf(0)).add(revenue));

                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown");

                countryRevenues.put(country, countryRevenues.getOrDefault(country, BigDecimal.valueOf(0)).add(revenue));

                String subcategory = "";

                String vendorName = "";

                if (booking.getAttraction() != null) {

                    subcategory = String.valueOf(booking.getAttraction().getAttraction_category());

                    vendorName = vendorRepository.findVendorByAttractionName(booking.getAttraction().getName()).getBusiness_name();

                } else if (booking.getRoom() != null) {

                    //String accommodation = accommodationRepository.getAccomodationByRoomId(booking.getRoom().getRoom_id());

                    subcategory = String.valueOf(booking.getRoom().getRoom_type()); // To change to accoms

                    Vendor vendor = vendorRepository.findVendorByRoomId(booking.getRoom().getRoom_id());


                    vendorName = vendor.getBusiness_name();

                } else if (booking.getTour() != null) {

                    subcategory = "Tour"; // Get tour's attraction

                    vendorName = localRepository.findLocalByTour(booking.getTour()).getName();


                } else if (booking.getTelecom() != null) {

                    subcategory = booking.getTelecom().getName();

                    vendorName = vendorRepository.findVendorByTelecomName(booking.getTelecom().getName()).getBusiness_name();

                } else if (booking.getItem() != null) {

                    subcategory = "Item";

                    vendorName = vendorRepository.findVendorByItemId(booking.getItem().getItem_id()).getBusiness_name();

                }

                subcategoryRevenues.put(subcategory , subcategoryRevenues.getOrDefault(subcategory , BigDecimal.valueOf(0)).add(revenue));

                vendorRevenues.put(vendorName, vendorRevenues.getOrDefault(vendorName, BigDecimal.valueOf(0)).add(revenue));

                String bookingStatus = String.valueOf(booking.getStatus());

                statusRevenues.put(bookingStatus, statusRevenues.getOrDefault(bookingStatus, BigDecimal.valueOf(0)).add(revenue));


            }

            result.put("Category", categoryRevenues);
            result.put("Country", countryRevenues);
            result.put("Status", statusRevenues);
            result.put("Subcategory", subcategoryRevenues);
            result.put("Vendor", vendorRevenues);

            System.out.println(result);

            List<Object> placeholderList = new ArrayList<>();

            placeholderList.add(result);

            dateCountryRevenueList.add(placeholderList);


            System.out.println(dateCountryRevenueList);

            return dateCountryRevenueList;
        } else if (Objects.equals(data_usecase, "Customer Retention Over Time")) {







// Create a map to store the booking count, revenue, and country for each date
            Map<LocalDate, List<Object>> bookingDataByDate = new HashMap<>();

            for (Booking booking : bookings) {
                LocalDate bookingDate = booking.getStart_datetime().toLocalDate();
                BigDecimal revenue = booking.getPayment().getPayment_amount();
                String countryCode = null;

                // Determine which user type (tourist or local) is not null and get the country code
                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown"); // Default to "Unknown" if not found in the mapping

                // Check if the booking date is already in the map
                if (bookingDataByDate.containsKey(bookingDate)) {
                    List<Object> existingData = bookingDataByDate.get(bookingDate);
                    int currentCount = (int) existingData.get(0);
                    BigDecimal currentRevenue = (BigDecimal) existingData.get(1);

                    // Update the count and revenue
                    existingData.set(0, currentCount + 1);
                    existingData.set(1, currentRevenue.add(revenue));
                } else {
                    // Initialize the list with count, revenue, and country
                    List<Object> newData = new ArrayList<>();
                    newData.add(1); // Count for the first booking on this date
                    newData.add(revenue);
                    newData.add(country);
                    bookingDataByDate.put(bookingDate, newData);
                }
            }


            List<Map.Entry<LocalDate, List<Object>>> sortedEntries = bookingDataByDate.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            List<List<Object>> dateBookingDataList = new ArrayList<>();
            for (Map.Entry<LocalDate, List<Object>> entry : sortedEntries) {
                LocalDate date = entry.getKey();
                List<Object> data = entry.getValue();
                dateBookingDataList.add(Arrays.asList(date, data.get(0), data.get(1), data.get(2)));
            }

            System.out.println(dateBookingDataList);

            return dateBookingDataList;



        }

//        } else if (Objects.equals(data_usecase, "Vendor Retention (Number of Repeat Bookings Over Time)")) {
//
//        }
        throw new NotFoundException("Data Use Case Not Found");

    }



}
