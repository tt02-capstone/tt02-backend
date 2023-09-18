package com.nus.tt02backend.services;

import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PaymentService {

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    UserRepository userRepository;


    public String retrieveStripeId(String user, String field) {

        if (user.equals("TOURIST")) {
            return touristRepository.getStripeIdByEmail(field);
        } else if (user.equals("LOCAL")) {

            return localRepository.getStripeIdByEmail(field);
        }

        return "";
    }

    public PaymentMethod retrievePaymentMethod(String payment_method_id) throws StripeException {

        return PaymentMethod.retrieve(
                payment_method_id
        );
    }

    public String createStripeAccount(String account_type, Map<String,Object> parameters) {

        try {
            if (Objects.equals(account_type, "CUSTOMER")) {
                Customer customer = Customer.create(parameters);

                return customer.getId();
            } else if (Objects.equals(account_type, "VENDOR")) {
                Account account = Account.create(parameters);

                return account.getId();
            }

        } catch (Exception e) {
            // handle error
            return null;
        }
        return null;
    }


    public List<PaymentMethod> getPaymentMethods(String user_type, String tourist_email) throws StripeException {
        System.out.println(tourist_email);
        String tourist_stripe_id = retrieveStripeId(user_type,tourist_email);
        System.out.println(tourist_stripe_id);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", tourist_stripe_id);
        params.put("type", "card");
        PaymentMethodCollection paymentMethods =
                PaymentMethod.list(params);

        return paymentMethods.getData();
    }


    public String addPaymentMethod(String user_type, String tourist_email,  String payment_method_id) throws StripeException {
        String tourist_stripe_id = retrieveStripeId(user_type, tourist_email);
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        Map<String, Object> params = new HashMap<>();

        params.put("customer", tourist_stripe_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.attach(params);

        return updatedPaymentMethod.getId();
    }

    public String deletePaymentMethod(String user_type, String tourist_email,  String payment_method_id
    ) throws StripeException {
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.detach();
        return updatedPaymentMethod.getId();
    }

    public String updatePaymentMethod(String payment_method_id, Integer exp_month, Integer exp_year) throws StripeException {
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);

        Map<String, Object> card = new HashMap<>();
        card.put("exp_month", exp_month);
        card.put("exp_year", exp_year);
        Map<String, Object> params = new HashMap<>();
        params.put("card", card);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.update(params);
        return updatedPaymentMethod.getId();
    }



}