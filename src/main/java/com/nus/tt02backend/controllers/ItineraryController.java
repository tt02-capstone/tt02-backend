package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.services.DIYEventService;
import com.nus.tt02backend.services.ItineraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/itinerary")
public class ItineraryController {
    @Autowired
    ItineraryService itineraryService;

}
