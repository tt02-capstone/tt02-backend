package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.RecommendationResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.ListingTypeEnum;
import com.nus.tt02backend.services.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/recommendation")
public class RecommendationController {

    @Autowired
    RecommendationService recommendationService;

    @GetMapping("/getRecommendation/{location}/{listingType}/{typeId}")
    public ResponseEntity<List<Object>> getRecommendation(@PathVariable GenericLocationEnum location, @PathVariable ListingTypeEnum listingType, @PathVariable Long typeId) throws NotFoundException {
        List<Object> list = recommendationService.getRecommendation(location,listingType,typeId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getPostRecommendation/{catId}")
    public ResponseEntity<List<Object>> getPostRecommendation(@PathVariable long catId) throws NotFoundException {
        List<Object> list = recommendationService.getPostRecommendation(catId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getRecommendationFromBookings/{userId}")
    public RecommendationResponse getRecommendationFromBookings(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        RecommendationResponse recommendation= recommendationService.getRecommendationFromBookings(userId);
        return recommendation;
    }
}
