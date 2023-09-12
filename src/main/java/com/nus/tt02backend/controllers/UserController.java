package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/login/{email}/{password}")
    public ResponseEntity<User> userLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        User user = userService.userLogin(email, password);
        return ResponseEntity.ok(user);
    }

    @PutMapping ("/update")
    public ResponseEntity<Void> updateUser(@RequestBody User userToUpdate) throws NotFoundException {
        userService.updateUser(userToUpdate);
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/passwordResetStageOne/{email}")
    public ResponseEntity<String> passwordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = userService.passwordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token)
            throws BadRequestException {
        String successMessage = userService.passwordResetStageTwo(token);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageThree/{token}/{password}")
    public ResponseEntity<String> passwordResetStageThree(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = userService.passwordResetStageThree(token, password);
        return ResponseEntity.ok(successMessage);
    }
}
