package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
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

    @GetMapping("/getLocalProfile/{localId}")
    public ResponseEntity<Local> getLocalProfile(@PathVariable Long localId) throws LocalNotFoundException {
        Local local = localService.getLocalProfile(localId);
        return ResponseEntity.ok(local);
    }

    @PutMapping("/editLocalProfile")
    public ResponseEntity<Local> editLocalProfile(@RequestBody Local localToEdit) throws EditUserException {
        Local local = localService.editLocalProfile(localToEdit);
        return ResponseEntity.ok(local);
    }

    @PutMapping("/editLocalPassword/{localId}/{oldPassword}/{newPassword}")
    public void editLocalPassword(@PathVariable Long localId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
        localService.editLocalPassword(localId, oldPassword, newPassword);
    }

    @PostMapping("/login/{email}/{password}")
    public ResponseEntity<Local> localLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        Local local = localService.localLogin(email, password);
        return ResponseEntity.ok(local);
    }

    @PutMapping ("/update")
    public ResponseEntity<Void> updateLocal(@RequestBody Local localToUpdate) throws NotFoundException {
        localService.updateLocal(localToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/create")
    public ResponseEntity<Long> createLocal(@RequestBody Local localToCreate) throws BadRequestException {
        Long localId = localService.createLocal(localToCreate);
        return ResponseEntity.ok(localId);
    }
}