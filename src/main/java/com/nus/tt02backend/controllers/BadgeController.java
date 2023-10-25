package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Badge;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.services.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/badge")
public class BadgeController {
    @Autowired
    BadgeService badgeService;

    @GetMapping("/awardedNewBadge/{userId}")
    public ResponseEntity<Badge> awardedNewBadge(@PathVariable Long userId) throws BadRequestException {
        Badge newBadge = badgeService.awardedNewBadge(userId);
        return ResponseEntity.ok(newBadge);
    }

    @GetMapping("/retrieveBadgesByUserId/{userId}")
    public ResponseEntity<List<Badge>> retrieveBadgesByUserId(@PathVariable Long userId) throws BadRequestException {
        List<Badge> badges = badgeService.retrieveBadgesByUserId(userId);
        return ResponseEntity.ok(badges);
    }
}
