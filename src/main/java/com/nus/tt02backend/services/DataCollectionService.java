package com.nus.tt02backend.services;


import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DataCollectionService {
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

    public String handleStripeWebhook(HttpServletRequest request) {
        return null;
    }



    public String createSubscription(String user_id, String user_type, String subscription_type, Boolean auto_renew) throws StripeException, NotFoundException {

        String stripe_account_id = getStripe_Id(user_id, user_type);

        List<Object> items = new ArrayList<>();

        Map<String, Object> subscription_params = new HashMap<>();
        Map<String, Object> transaction_params = new HashMap<>();
        subscription_params.put("customer", stripe_account_id);

        Customer customer =
                Customer.retrieve(stripe_account_id);

        Map<String, String> subscription_price = new HashMap<>();
        if (Objects.equals(subscription_type, "MONTHLY")) {

            subscription_price .put(
                    "price",
                    "price_1O1Pf2JuLboRjh4qv1wswh2w"
            );

        } else if (Objects.equals(subscription_type, "YEARLY")) {

            subscription_price.put(
                    "price",
                    "price_1O1PfLJuLboRjh4qj2lYrFHi"
            );

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

    public String updateSubscription(String user_id, String user_type, String subscription_type, Boolean auto_renew) throws StripeException {

        List<Object> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put(
                "price",
                "price_1NyRraJnvmXwwenzwhdWEwo3"
        );
        items.add(item1);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", "cus_ObWImLy0HaffAq");
        params.put("items", items);

        Subscription subscription =
                Subscription.create(params);

        return "";
    }

    public String cancelSubscription() throws StripeException {

        List<Object> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put(
                "price",
                "price_1NyRraJnvmXwwenzwhdWEwo3"
        );
        items.add(item1);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", "cus_ObWImLy0HaffAq");
        params.put("items", items);

        Subscription subscription =
                Subscription.create(params);

        return "";
    }

    public String checkSubscription() {
        return "";
    }

    public String getSubscription() {
        return "";
    }

}
