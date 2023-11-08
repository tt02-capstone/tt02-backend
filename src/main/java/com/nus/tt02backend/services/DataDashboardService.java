package com.nus.tt02backend.services;


import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Booking;
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

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

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

                List<Booking> bookings = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

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

        } else if (Objects.equals(data_usecase, "Bookings Breakdown by Activity, Nationality, Age")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

                Map<LocalDate, Integer> dateCounts = new HashMap<>();
                List<List<Object>> dateCountryRevenueList = new ArrayList<>();

                Map<String, Object> result = new HashMap<>();
                Map<String, Integer> categoryCounts = new HashMap<>();
                Map<String, Integer> countryCounts = new HashMap<>();
                Map<String, Integer> statusCounts = new HashMap<>();

                for (Booking booking : bookings) {

                    String countryCode = null;
                    String roomType = String.valueOf(booking.getRoom().getRoom_type());
                    categoryCounts.put(roomType, categoryCounts.getOrDefault(roomType, 0) + 1);

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

                return dateCountryRevenueList;

            } else {
                throw new NotFoundException("Vendor not found");
            }

        } else if (Objects.equals(data_usecase, "Revenue Breakdown by Activity, Nationality, Age")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {

                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

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
                    String roomType = String.valueOf(booking.getRoom().getRoom_type());
                    categoryRevenues.put(roomType, categoryRevenues.getOrDefault(roomType, BigDecimal.valueOf(0)).add(revenue));

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

                return dateCountryRevenueList;

            } else {
                throw new NotFoundException("Vendor not found");
            }


        } else if (Objects.equals(data_usecase, "Customer Retention (Number of Repeat Bookings Over Time)")) {

            Optional<Vendor> vendorOptional = vendorRepository.findById(Long.valueOf(vendorId));
            if (vendorOptional.isPresent()) {



                Vendor vendor = vendorOptional.get();

                List<Accommodation> accommodations = vendor.getAccommodation_list();

                LocalDateTime startDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 1), LocalTime.MIDNIGHT);

                LocalDateTime endDate = LocalDateTime.of(LocalDate.ofYearDay(2023, 304), LocalTime.MIDNIGHT);

                List<Booking> bookings = bookingRepository.getBookingsOverTime(startDate, endDate, 1L, "ACCOMMODATION");

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

        List<Booking> bookings = bookingRepository.getPlatformBookingsOverTime(startDate, endDate);

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

                // Create [Date, Country] pair and add to the list
                List<Object> dateCountryPair = Arrays.asList(bookingDate, country);
                dateCountryList.add(dateCountryPair);
            }

            System.out.println(dateCountryList);

            return dateCountryList;

        } else if (Objects.equals(data_usecase, "Platform Revenue Over Time")) {

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

                // To add vendors
            }

            System.out.println(dateCountryRevenueList);

            return dateCountryRevenueList;

        } else if (Objects.equals(data_usecase, "Platform Bookings Breakdown by Category, Nationality, Status")) {

            Map<LocalDate, Integer> dateCounts = new HashMap<>();
            List<List<Object>> dateCountryRevenueList = new ArrayList<>();

            Map<String, Object> result = new HashMap<>();
            Map<String, Integer> categoryCounts = new HashMap<>();
            Map<String, Integer> countryCounts = new HashMap<>();
            Map<String, Integer> statusCounts = new HashMap<>();

            for (Booking booking : bookings) {

                String countryCode = null;
                String roomType = String.valueOf(booking.getRoom().getRoom_type());
                categoryCounts.put(roomType, categoryCounts.getOrDefault(roomType, 0) + 1);

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

            return dateCountryRevenueList;

        } else if (Objects.equals(data_usecase, "Platform Revenue Breakdown by Category, Nationality, Status")) {

            Map<String, Object> result = new HashMap<>();
            List<List<Object>> dateCountryRevenueList = new ArrayList<>();
            Map<String, BigDecimal> categoryRevenues = new HashMap<>();
            Map<String, BigDecimal> countryRevenues = new HashMap<>();
            Map<String, BigDecimal> statusRevenues = new HashMap<>();

            for (Booking booking : bookings) {

                BigDecimal commission = booking.getPayment().getPayment_amount().multiply(BigDecimal.valueOf(0.1));
                BigDecimal revenue = booking.getPayment().getPayment_amount().subtract(commission);

                String countryCode = null;
                String roomType = String.valueOf(booking.getRoom().getRoom_type());
                categoryRevenues.put(roomType, categoryRevenues.getOrDefault(roomType, BigDecimal.valueOf(0)).add(revenue));

                if (booking.getTourist_user() != null && booking.getTourist_user().getCountry_code() != null) {
                    countryCode = booking.getTourist_user().getCountry_code();
                } else if (booking.getLocal_user() != null && booking.getLocal_user().getCountry_code() != null) {
                    countryCode = booking.getLocal_user().getCountry_code();
                }

                String country = countryCodeToCountry.getOrDefault(countryCode, "Unknown");

                countryRevenues.put(country, countryRevenues.getOrDefault(country, BigDecimal.valueOf(0)).add(revenue));

                String bookingStatus = String.valueOf(booking.getStatus());

                statusRevenues.put(bookingStatus, statusRevenues.getOrDefault(bookingStatus, BigDecimal.valueOf(0)).add(revenue));


            }

            result.put("Category", categoryRevenues);
            result.put("Country", countryRevenues);
            result.put("Status", statusRevenues);

            System.out.println(result);

            return dateCountryRevenueList;
        }

//        } else if (Objects.equals(data_usecase, "Vendor Retention (Number of Repeat Bookings Over Time)")) {
//
//        }
        throw new NotFoundException("Data Use Case Not Found");

    }



}
