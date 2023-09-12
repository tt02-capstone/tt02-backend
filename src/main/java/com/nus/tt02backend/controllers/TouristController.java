package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.services.TouristService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/tourist")
public class TouristController {
    @Autowired
    TouristService touristService;

    @PostMapping("/login/{email}/{password}")
    public ResponseEntity<Tourist> touristLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        Tourist tourist = touristService.touristLogin(email, password);
        return ResponseEntity.ok(tourist);
    }

    @PutMapping ("/update")
    public ResponseEntity<Void> updateTourist(@RequestBody Tourist touristToUpdate) throws NotFoundException {
        touristService.updateTourist(touristToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createTourist(@RequestBody Tourist touristToCreate) throws BadRequestException {
        Long touristId = touristService.createTourist(touristToCreate);
        return ResponseEntity.ok(touristId);
    }

    @PostMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = touristService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token)
            throws BadRequestException {
        String successMessage = touristService.passwordResetStageTwo(token);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageThree/{token}/{password}")
    public ResponseEntity<String> passwordResetStageThree(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = touristService.passwordResetStageThree(token, password);
        return ResponseEntity.ok(successMessage);
    }
}
