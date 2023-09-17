package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.services.AuthenticationService;
import com.nus.tt02backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    UserService userService;
    @PostMapping("/mobileLogin/{email}/{password}")
    public ResponseEntity<JwtAuthenticationResponse> userMobileLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        JwtAuthenticationResponse userResponse = authenticationService.userMobileLogin(email, password);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/webLogin/{email}/{password}")
    public ResponseEntity<JwtAuthenticationResponse> userWebLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        JwtAuthenticationResponse userResponse = authenticationService.userWebLogin(email, password);
        return ResponseEntity.ok(userResponse);
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

    @PostMapping ("/webPasswordResetStageOne/{email}")
    public ResponseEntity<String> webPasswordResetStageOne(@PathVariable String email) throws BadRequestException {
        String successMessage = userService.webPasswordResetStageOne(email);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageTwo/{token}")
    public ResponseEntity<String> passwordResetStageTwo(@PathVariable String token)
            throws BadRequestException {
        String successMessage = userService.passwordResetStageTwo(token);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/webPasswordResetStageTwo/{email}/{token}")
    public ResponseEntity<String> webPasswordResetStageTwo(@PathVariable String email, @PathVariable String token)
            throws BadRequestException {
        String successMessage = userService.webPasswordResetStageTwo(email, token);
        return ResponseEntity.ok(successMessage);
    }

    @PostMapping ("/passwordResetStageThree/{token}/{password}")
    public ResponseEntity<String> passwordResetStageThree(@PathVariable String token, @PathVariable String password)
            throws BadRequestException {
        String successMessage = userService.passwordResetStageThree(token, password);
        return ResponseEntity.ok(successMessage);
    }

    @PutMapping("/editPassword/{userId}/{oldPassword}/{newPassword}")
    public void editPassword(@PathVariable Long userId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
        System.out.println("edit pass");
        userService.editPassword(userId, oldPassword, newPassword);
    }

    // only for admin portal, not vendor portal
    @PutMapping("/toggleBlock/{userId}")
    public void toggleBlock(@PathVariable Long userId) throws NotFoundException, ToggleBlockException {
        userService.toggleBlock(userId);
    }

    @PostMapping ("/webPasswordResetStageThree/{email}/{password}")
    public ResponseEntity<String> webPasswordResetStageThree(@PathVariable String email, @PathVariable String password)
            throws BadRequestException {
        String successMessage = userService.webPasswordResetStageThree(email, password);
        return ResponseEntity.ok(successMessage);
    }
}
