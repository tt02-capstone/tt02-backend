package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Tourist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/tourist")
public class TouristController {
    @Autowired
    TouristServiceImpl touristService;

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
}
