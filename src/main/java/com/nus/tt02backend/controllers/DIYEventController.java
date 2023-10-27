package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.services.DIYEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/diyEvent")
public class DIYEventController {
    @Autowired
    DIYEventService diyEventService;

    @GetMapping("/getAllDiyEvents/{itineraryId}")
    public ResponseEntity<List<DIYEvent>> getAllDiyEvents(@PathVariable Long itineraryId) throws NotFoundException, BadRequestException {
        List<DIYEvent> diyEventList = diyEventService.getAllDiyEvents(itineraryId);
        return ResponseEntity.ok(diyEventList);
    }

    @GetMapping("/getDiyEvent/{diyEventId}")
    public ResponseEntity<DIYEvent> getDiyEvent(@PathVariable Long diyEventId) throws NotFoundException {
        DIYEvent diyEvent = diyEventService.getDiyEvent(diyEventId);
        return ResponseEntity.ok(diyEvent);
    }

    @GetMapping("/getAllDiyEventsByDay/{itineraryId}/{dayNumber}")
    public ResponseEntity<List<DIYEvent>> getAllDiyEventsByDay(@PathVariable Long itineraryId, @PathVariable Long dayNumber) throws NotFoundException, BadRequestException {
        List<DIYEvent> diyEventList = diyEventService.getAllDiyEventsByDay(itineraryId, dayNumber);
        return ResponseEntity.ok(diyEventList);
    }

    @PostMapping("/createDiyEvent/{itineraryId}/{typeId}/{type}")
    public ResponseEntity<DIYEvent> createDiyEvent(@PathVariable Long itineraryId,
                                                   @PathVariable Long typeId,
                                                   @PathVariable String type,
                                                   @RequestBody DIYEvent diyEventToCreate) throws BadRequestException {
        DIYEvent newDiyEvent = diyEventService.createDiyEvent(itineraryId, typeId, type, diyEventToCreate);
        return ResponseEntity.ok(newDiyEvent);
    }

    @PutMapping("/updateDiyEvent")
    public ResponseEntity<DIYEvent> updateDiyEvent(@RequestBody DIYEvent diyEventToUpdate) throws BadRequestException {
        DIYEvent updatedDiyEvent = diyEventService.updateDiyEvent(diyEventToUpdate);
        return ResponseEntity.ok(updatedDiyEvent);
    }

    @DeleteMapping("/deleteDiyEvent/{diyEventIdToDelete}")
    public ResponseEntity<String> deleteDiyEvent(@PathVariable Long diyEventIdToDelete) throws BadRequestException {
        String responseMessage = diyEventService.deleteDiyEvent(diyEventIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }
}
