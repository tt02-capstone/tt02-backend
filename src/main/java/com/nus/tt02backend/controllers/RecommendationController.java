package com.nus.tt02backend.controllers;

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
    public ResponseEntity<List<Object>> getRecommendation(@PathVariable GenericLocationEnum location, @PathVariable ListingTypeEnum listingType, @PathVariable Long typeId) throws NotFoundException {System.out.println("in controller");
        List<Object> rList = recommendationService.getRecommendation(location,listingType,typeId);
        return ResponseEntity.ok(rList);
    }
}