package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.services.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/local")
public class LocalController {

    @Autowired
    LocalService localService;

    @PutMapping("/editLocalProfile")
    @PreAuthorize("hasRole('LOCAL') ")
    public ResponseEntity<Local> editLocalProfile(@RequestBody Local localToEdit) throws EditUserException {
        Local local = localService.editLocalProfile(localToEdit);
        return ResponseEntity.ok(local);
    }

    @PutMapping ("/update")
    @PreAuthorize("hasRole('LOCAL') ")
    public ResponseEntity<Void> updateLocal(@RequestBody Local localToUpdate) throws NotFoundException {
        localService.updateLocal(localToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createLocal(@RequestBody Local localToCreate) throws BadRequestException {
        Long localId = localService.createLocal(localToCreate);
        return ResponseEntity.ok(localId);
    }

    @GetMapping("/getAllLocal")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public ResponseEntity<List<Local>> getAllLocal() {
        List<Local> locaList = localService.retrieveAllLocal();
        return ResponseEntity.ok(locaList);
    }


}