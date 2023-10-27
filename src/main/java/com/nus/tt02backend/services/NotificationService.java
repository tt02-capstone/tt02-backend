package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.NotificationRequest;
import com.nus.tt02backend.dto.NotificationResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.time.*;
import java.util.*;

@Service
public class NotificationService {

    @Autowired
    DIYEventRepository diyEventRepository;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;

//    appId: 13960,
//    appToken: "BEbA270k2T53VV6Cu8pZIZ",
//    title: "Push title here as a string",
//    body: "Push message here as a string",
//    dateSent: "10-27-2023 8:54PM",
//    pushData: { yourProperty: "yourPropertyValue" },
//    bigPictureURL: Big picture URL as a string

    public void sendManualNotification(NotificationRequest notificationRequest, Long userId) throws NotFoundException, BadRequestException {

        LocalDateTime startDateTime = notificationRequest.getDate();
        LocalDateTime endDateTime = startDateTime.plusHours(1);
        List<DIYEvent> list = diyEventRepository.getDiyEventByDate(startDateTime, endDateTime);

        String concat = "";
        boolean firstItem = true;

        for (DIYEvent d : list) {
//            System.out.println("event id: " + d.getDiy_event_id() + ", " + d.getStart_datetime());
            if (firstItem) {
                concat = d.getName();
                firstItem = false;
            } else {
                concat = concat + " and " + d.getName();
            }
        }

        String uri = "https://app.nativenotify.com/api/notification";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setAppId(13960);
        notificationResponse.setAppToken("BEbA270k2T53VV6Cu8pZIZ");
        notificationResponse.setTitle("WithinSG Notification");
        notificationResponse.setBody(concat + " is about to start. Are you excited?" );
        notificationResponse.setDateSent(new Date());

        HttpEntity<NotificationResponse> entity = new HttpEntity<>(notificationResponse, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);

        for (DIYEvent d : list) {
            this.createNotification(d, userId);
        }
    }

    // sec-min-hour-day-month
    @Scheduled(cron = "0 0 * * *")
    public void sendScheduledNotification() throws NotFoundException, BadRequestException {
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusHours(1);
        List<DIYEvent> list = diyEventRepository.getDiyEventByDate(startDateTime, endDateTime);

        String concat = "";
        boolean firstItem = true;

        for (DIYEvent d : list) {
            if (firstItem) {
                concat = d.getName();
                firstItem = false;
            } else {
                concat = concat + " and " + d.getName();
            }
        }

        String uri = "https://app.nativenotify.com/api/notification";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setAppId(13960);
        notificationResponse.setAppToken("BEbA270k2T53VV6Cu8pZIZ");
        notificationResponse.setTitle("WithinSG Notification");
        notificationResponse.setBody(concat + " is about to start. Are you excited?" );
        notificationResponse.setDateSent(new Date());

        HttpEntity<NotificationResponse> entity = new HttpEntity<>(notificationResponse, headers);
        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);

        for (DIYEvent d : list) {
            List<Tourist> touristList = touristRepository.getTouristIdFromDIYEvent();
            for (Tourist t : touristList) {
                for (DIYEvent e : t.getItinerary().getDiy_event_list()) {
                    if (d.getDiy_event_id().equals(e.getDiy_event_id())) {
                        this.createNotification(d, t.getUser_id());
                    }
                }
            }
        }

        for (DIYEvent d : list) {
            List<Local> localList = localRepository.getLocalIdFromDIYEvent();
            for (Local l : localList) {
                for (DIYEvent e : l.getItinerary().getDiy_event_list()) {
                    if (d.getDiy_event_id().equals(e.getDiy_event_id())) {
                        this.createNotification(d, l.getUser_id());
                    }
                }
            }
        }
    }

    private void createNotification(DIYEvent event, Long userId) throws NotFoundException, BadRequestException {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new NotFoundException();

        User user = userOptional.get();
        Notification notification = new Notification();
        notification.setTitle("WithinSG Notification");
        notification.setBody(event.getName());
        notification.setIs_read(false);
        notification.setCreated_datetime(LocalDateTime.now());
        notificationRepository.save(notification);

        if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            if (tourist.getNotification_list() == null) tourist.setNotification_list(new ArrayList<>());
            tourist.getNotification_list().add(notification);
            touristRepository.save(tourist);

        } else if (user instanceof Local) {
            Local local = (Local) user;
            if (local.getNotification_list() == null) local.setNotification_list(new ArrayList<>());
            local.getNotification_list().add(notification);
            localRepository.save(local);

        } else {
            throw new BadRequestException("User is not a tourist or local!");
        }
    }

    public List<Notification> getUserNotification(Long userId) throws NotFoundException, BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new NotFoundException("User not found!");

        User user = userOptional.get();
        if (user instanceof Tourist) {
            Tourist tourist = (Tourist) user;
            return tourist.getNotification_list();

        } else if (user instanceof Local) {
            Local local = (Local) user;
            return local.getNotification_list();
        } else {
            throw new BadRequestException("User is not local or tourist!");
        }
    }

    public void updateNotification(Long notificationId) throws NotFoundException {
        Optional<Notification> notificationOptional = notificationRepository.findById(notificationId);
        if (notificationOptional.isEmpty()) throw new NotFoundException("Notification not found!");

        Notification notification = notificationOptional.get();
        notification.setIs_read(true);
        notificationRepository.save(notification);
    }
}
