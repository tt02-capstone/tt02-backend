package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Telecom;
import com.nus.tt02backend.services.AttractionService;
import com.nus.tt02backend.services.TelecomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/telecom")
public class TelecomController {
    @Autowired
    AttractionService attractionService;
    @Autowired
    TelecomService telecomService;

    @PostMapping("/create/{vendorId}")
    public ResponseEntity<Telecom> create(@RequestBody Telecom telecomToCreate, @PathVariable Long vendorId) throws NotFoundException {
        System.out.println(telecomToCreate);
        Telecom telecom = telecomService.create(telecomToCreate, vendorId);
        return ResponseEntity.ok(telecom);
    }

    @GetMapping("/getAllTelecomList")
    public ResponseEntity<List<Telecom>> getAllTelecomeList() {
        List<Telecom> telecomList = telecomService.getAllTelecomList();
        return ResponseEntity.ok(telecomList);
    }

    @GetMapping("/getAssociatedTelecomList/{vendorId}")
    public ResponseEntity<List<Telecom>> getAssociatedTelecomList(@PathVariable Long vendorId) throws NotFoundException {
        List<Telecom> telecomList = telecomService.getAllAssociatedTelecom(vendorId);
        return ResponseEntity.ok(telecomList);
    }

    @GetMapping("/getTelecomById/{telecomId}")
    public ResponseEntity<Telecom> getTelecomById(@PathVariable Long telecomId) throws NotFoundException {
        Telecom telecom = telecomService.getTelecomById(telecomId);
        return ResponseEntity.ok(telecom);
    }

    @GetMapping("/getPublishedTelecomList")
    public ResponseEntity<List<Telecom>> getPublishedTelecomList() {
        List<Telecom> list = telecomService.getPublishedTelecomList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/update")
    public ResponseEntity<Telecom> update(@RequestBody Telecom telecomToEdit) throws NotFoundException {
        Telecom telecom = telecomService.update(telecomToEdit);
        return ResponseEntity.ok(telecom);
    }

    @PutMapping("/toggleSaveTelecom/{userId}/{telecomId}")
    public ResponseEntity<List<Telecom>> toggleSaveTelecom(@PathVariable Long userId, @PathVariable Long telecomId) throws NotFoundException {
        List<Telecom> list = telecomService.toggleSaveTelecom(userId, telecomId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getUserSavedTelecom/{userId}")
    public ResponseEntity<List<Telecom>> getPublishedTelecomList(@PathVariable Long userId) throws NotFoundException {
        List<Telecom> list = telecomService.getUserSavedTelecom(userId);
        return ResponseEntity.ok(list);
    }
}
