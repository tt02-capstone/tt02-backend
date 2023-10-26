package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.services.DIYEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/diyEvent")
public class DIYEventController {
    @Autowired
    DIYEventService diyEventService;

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
