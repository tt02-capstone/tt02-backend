package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.services.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/local")
public class LocalController {

    @Autowired
    LocalService localService;

    @PostMapping("/create")
    public ResponseEntity<Long> createLocal(@RequestBody Local localToCreate) throws BadRequestException {
        Long localId = localService.createLocal(localToCreate);
        return ResponseEntity.ok(localId);
    }
}
