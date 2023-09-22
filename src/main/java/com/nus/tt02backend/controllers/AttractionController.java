package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.TicketPerDay;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.TicketEnum;
import com.nus.tt02backend.services.AttractionService;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/attraction")
public class AttractionController {
    @Autowired
    AttractionService attractionService;

    @GetMapping("/getAllAttraction")
    public ResponseEntity<List<Attraction>> getAttractionList() {
        List<Attraction> attractionList = attractionService.retrieveAllAttraction();
        return ResponseEntity.ok(attractionList);
    }

    @GetMapping("/getAllPublishedAttraction") // local n tourist can only view published listings
    public ResponseEntity<List<Attraction>> getPublishedAttractionList() {
        List<Attraction> publishedAttractionList = attractionService.retrieveAllPublishedAttraction();
        return ResponseEntity.ok(publishedAttractionList);
    }

    @GetMapping("/getAttraction/{attractionId}")
    public ResponseEntity<Attraction> getAttraction(@PathVariable Long attractionId) throws NotFoundException {
        Attraction attraction = attractionService.retrieveAttraction(attractionId);
        return ResponseEntity.ok(attraction);
    }

    @GetMapping("/getAttractionListByVendor/{vendorStaffId}")
    public ResponseEntity<List<Attraction>> getAttractionListByVendor(@PathVariable Long vendorStaffId) throws NotFoundException {
        List<Attraction> attractionList = attractionService.retrieveAllAttractionByVendor(vendorStaffId);
        return ResponseEntity.ok(attractionList);
    }

    @GetMapping("/getAttractionByVendor/{vendorStaffId}/{attractionId}")
    public ResponseEntity<Attraction> getAttractionByVendor(@PathVariable Long vendorStaffId,@PathVariable Long attractionId) throws NotFoundException {
        Attraction attraction = attractionService.retrieveAttractionByVendor(vendorStaffId,attractionId);
        return ResponseEntity.ok(attraction);
    }

    @PostMapping ("createAttraction/{vendorStaffId}")
    public ResponseEntity<Attraction> createAttraction(@PathVariable Long vendorStaffId ,@RequestBody Attraction attractionToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException  {

        VendorStaff vendorStaff = attractionService.retrieveVendor(vendorStaffId);
        Attraction attraction =  attractionService.createAttraction(vendorStaff,attractionToCreate);
        return ResponseEntity.ok(attraction);
    }

    @PutMapping("/updateAttraction/{vendorStaffId}")
    public ResponseEntity<Void> updateAttraction(@PathVariable Long vendorStaffId ,@RequestBody Attraction attractionToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = attractionService.retrieveVendor(vendorStaffId);
        attractionService.updateAttraction(vendorStaff, attractionToUpdate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAttractionRecommendation/{currentAttractionId}")
    public ResponseEntity<List<Attraction>> getAttractionByVendor(@PathVariable Long currentAttractionId) throws NotFoundException {
        List<Attraction> attractionRecommendationList = attractionService.relatedAttractionRecommendation(currentAttractionId);
        return ResponseEntity.ok(attractionRecommendationList);
    }

    @GetMapping("/getSavedAttractionListForTouristAndLocal/{userId}")
    public ResponseEntity<List<Attraction>> getSavedAttractionListForTouristAndLocal(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Attraction> savedAttractionList = attractionService.retrieveAllSavedAttractionsForTouristAndLocal(userId);
        return ResponseEntity.ok(savedAttractionList);
    }

    @PutMapping("/saveAttractionForTouristAndLocal/{userId}/{currentAttractionId}")
    public ResponseEntity<User> updateSavedAttractionListForTouristAndLocal(@PathVariable Long userId , @PathVariable Long currentAttractionId) throws BadRequestException, NotFoundException {
        User user = attractionService.saveAttractionForTouristAndLocal(userId, currentAttractionId);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/removeSavedAttractionForTouristAndLocal/{userId}/{currentAttractionId}")
    public ResponseEntity<User> removeSavedAttractionListForTouristAndLocal(@PathVariable Long userId , @PathVariable Long currentAttractionId) throws NotFoundException {
        User user = attractionService.removeSavedAttractionForTouristAndLocal(userId, currentAttractionId);
        return ResponseEntity.ok(user);
    }

    @PostMapping ("/createTicketsPerDay/{startDate}/{endDate}/{ticketType}/{ticketCount}/{attraction_id}")
    public ResponseEntity<List<TicketPerDay>> createTicketsPerDayList(@PathVariable LocalDate startDate, @PathVariable LocalDate endDate,
                                                          @PathVariable TicketEnum ticketType, @PathVariable int ticketCount,
                                                          @PathVariable Long attraction_id ) throws NotFoundException  {
        List<TicketPerDay> ticketList = attractionService.createTicketsPerDayList(startDate,endDate,ticketType,ticketCount,attraction_id);
        return ResponseEntity.ok(ticketList);
    }

    @PutMapping ("/updateTicketsPerDay/{attraction_id}")
    public ResponseEntity<List<TicketPerDay>> updateTicketsPerDay(@PathVariable Long attraction_id , @RequestBody TicketPerDay ticket_to_update) throws NotFoundException  {
        List<TicketPerDay> ticketList = attractionService.updateTicketsPerDay(attraction_id,ticket_to_update);
        return ResponseEntity.ok(ticketList);
    }

    @GetMapping("/getAllTicketListed")
    public ResponseEntity<List<TicketPerDay>> getAllTicketListed() {
        List<TicketPerDay> ticketList = attractionService.getAllTickets();
        return ResponseEntity.ok(ticketList);
    }

    @GetMapping("/getAllTicketListedByAttraction/{attraction_id}")
    public ResponseEntity<List<TicketPerDay>> getAllTicketListedByAttraction(@PathVariable Long attraction_id) throws NotFoundException {
        List<TicketPerDay> ticketList = attractionService.getAllTicketListedByAttraction(attraction_id);
        return ResponseEntity.ok(ticketList);
    }

    // for customer side
    @GetMapping("/getAllTicketListedByAttractionAndDate/{attraction_id}/{date_selected}")
    public ResponseEntity<List<TicketPerDay>> getAllTicketListedByAttractionAndDate(@PathVariable Long attraction_id, @PathVariable LocalDate date_selected) throws NotFoundException {
        List<TicketPerDay> ticketList = attractionService.getAllTicketListedByAttractionAndDate(attraction_id,date_selected);
        return ResponseEntity.ok(ticketList);
    }

    @GetMapping("/getTicketEnumByAttraction/{attraction_id}")
    public ResponseEntity<List<TicketEnum>> getTicketEnumByAttraction(@PathVariable Long attraction_id) throws NotFoundException {
        List<TicketEnum> ticketTypes = attractionService.getTicketEnumByAttraction(attraction_id);
        return ResponseEntity.ok(ticketTypes);
    }
    @PostMapping("/checkTicketInventory/{attraction_id}/{ticket_date}")
    public ResponseEntity<Void> checkTicketInventory(@PathVariable Long attraction_id, @PathVariable LocalDate ticket_date, @RequestBody List<TicketPerDay> tickets_to_check) throws BadRequestException ,NotFoundException {
        attractionService.checkTicketInventory(attraction_id,ticket_date,tickets_to_check);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getLastAttractionId")
    public ResponseEntity<?> getLastAttractionId() {
        try {
            Long lastAttractionId = attractionService.getLastAttractionId();
            return ResponseEntity.ok(lastAttractionId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
