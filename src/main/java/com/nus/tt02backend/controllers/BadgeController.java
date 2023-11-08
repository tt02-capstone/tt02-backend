package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.BadgeProgressResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Badge;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.enums.BadgeTypeEnum;
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

    @PutMapping("/markBadgeAsPrimary/{badgeId}/{userId}")
    public ResponseEntity<Badge> markBadgeAsPrimary(@PathVariable Long badgeId, @PathVariable Long userId) throws BadRequestException {
        Badge b = badgeService.markBadgeAsPrimary(badgeId,userId);
        return ResponseEntity.ok(b);
    }

    @GetMapping("/getPrimaryBadge/{userId}")
    public ResponseEntity<Badge> getPrimaryBadge(@PathVariable Long userId) throws BadRequestException {
        Badge b = badgeService.getPrimaryBadge(userId);
        return ResponseEntity.ok(b);
    }

    @GetMapping("/getAllBadgeTypes/{userId}")
    public ResponseEntity<List<BadgeTypeEnum>> getAllBadgeTypes(@PathVariable Long userId) throws NotFoundException {
        List<BadgeTypeEnum> badgeTypes = badgeService.getAllBadgeTypes(userId);
        return ResponseEntity.ok(badgeTypes);
    }

    @GetMapping("/getBadgeProgress/{userId}")
    public ResponseEntity<BadgeProgressResponse> getBadgeProgress(@PathVariable Long userId) throws NotFoundException {
        BadgeProgressResponse badgeProgress = badgeService.getBadgeProgress(userId);
        return ResponseEntity.ok(badgeProgress);
    }
}
