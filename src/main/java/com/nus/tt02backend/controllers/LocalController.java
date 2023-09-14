package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
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
