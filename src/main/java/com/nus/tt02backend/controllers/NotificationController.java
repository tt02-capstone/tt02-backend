package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.NotificationRequest;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Deal;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.services.DealService;
import com.nus.tt02backend.services.NotificationService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @PostMapping("/sendNotification")
    public void sendNotification(@RequestBody NotificationRequest notificationRequest) {
        notificationService.sendManualNotification(notificationRequest);
    }
}
