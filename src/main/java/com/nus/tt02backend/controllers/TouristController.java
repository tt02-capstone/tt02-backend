package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
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
    public ResponseEntity<Void> touristLogin(@RequestBody Tourist touristToUpdate) throws NotFoundException {
        touristService.updateTourist(touristToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createTourist(@RequestBody Tourist touristToCreate) throws BadRequestException {
        Long touristId = touristService.createTourist(touristToCreate);
        return ResponseEntity.ok(touristId);
    }

    @GetMapping("/getTouristProfile/{touristId}")
    public ResponseEntity<Tourist> getTouristProfile(@PathVariable Long touristId) throws TouristNotFoundException {
        Tourist tourist = touristService.getTouristProfile(touristId);
        return ResponseEntity.ok(tourist);
    }

    @PutMapping("/editTouristProfile")
    public ResponseEntity<Tourist> editTouristProfile(@RequestBody Tourist touristToEdit) throws EditUserException {
        Tourist tourist = touristService.editTouristProfile(touristToEdit);
        return ResponseEntity.ok(tourist);
    }

    @PutMapping("/editTouristPassword/{touristId}/{oldPassword}/{newPassword}")
    public void editTouristPassword(@PathVariable Long touristId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
        touristService.editTouristPassword(touristId, oldPassword, newPassword);
    }
}
