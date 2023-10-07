package com.nus.tt02backend.services;


import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCollectionService {



    public String addSubscription() throws StripeException {

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

    public String updateSubscription() throws StripeException {

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

}
