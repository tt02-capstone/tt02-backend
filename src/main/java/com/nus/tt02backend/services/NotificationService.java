package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.NotificationRequest;
import com.nus.tt02backend.dto.NotificationResponse;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.*;

@Service
public class NotificationService {

    @Autowired
    BookingRepository bookingRepository;

    public void sendManualNotification(NotificationRequest notificationRequest) {
        String uri = "https://app.nativenotify.com/api/notification";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setAppId(13960);
        notificationResponse.setAppToken("BEbA270k2T53VV6Cu8pZIZ");
        notificationResponse.setTitle("Backend title");
        notificationResponse.setBody("Backend body");
        notificationResponse.setDateSent(new Date());

        HttpEntity<NotificationResponse> entity = new HttpEntity<NotificationResponse>(notificationResponse, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class).getBody();
    }

    // sec-min-hour-day-month
    @Scheduled(cron = "0 0 * * *")
    public void sendScheduledNotification() {
        String uri = "https://app.nativenotify.com/api/notification";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setAppId(13960);
        notificationResponse.setAppToken("BEbA270k2T53VV6Cu8pZIZ");
        notificationResponse.setTitle("Backend title");
        notificationResponse.setBody("Backend body");
        notificationResponse.setDateSent(new Date());

        HttpEntity<NotificationResponse> entity = new HttpEntity<NotificationResponse>(notificationResponse, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class).getBody();
    }
}
