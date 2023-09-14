package com.nus.tt02backend.services;

import com.nus.tt02backend.repositories.TouristRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
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

    //@Autowired
    //LocalRepository localRepository;

    public String retrieveStripeId(String user, String field) {
        if (Objects.equals(user, "Tourist")) {
            return touristRepository.getStripeIdByEmail(field);
        }

        return "";
    }

    public PaymentMethod retrievePaymentMethod(String payment_method_id) throws StripeException {

        return PaymentMethod.retrieve(
                payment_method_id
        );
    }


    public List<PaymentMethod> getPaymentMethods(String tourist_email) throws StripeException {
        String tourist_stripe_id = retrieveStripeId("Tourist",tourist_email);
        // "cus_OalsHOTNEwycEX"
        Map<String, Object> params = new HashMap<>();
        params.put("customer", tourist_stripe_id);
        params.put("type", "card");
        PaymentMethodCollection paymentMethods =
                PaymentMethod.list(params);

        return paymentMethods.getData();
    }


    public String addPaymentMethod(String tourist_email, String payment_method_id) throws StripeException {
        String tourist_stripe_id = retrieveStripeId("Tourist",tourist_email);
        PaymentMethod paymentMethod = retrievePaymentMethod(payment_method_id);


        Map<String, Object> params = new HashMap<>();
        //To get user based on user_email and get their stripe_account_id
        String stripe_account_id = "cus_OalsHOTNEwycEX";

        params.put("customer", stripe_account_id);

        PaymentMethod updatedPaymentMethod =
                paymentMethod.attach(params);

        return updatedPaymentMethod.getId();
    }

    public String deletePaymentMethod(String payment_method_id) throws StripeException {
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
