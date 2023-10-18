package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.dto.JwtRefreshResponse;
import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.services.AuthenticationService;
import com.nus.tt02backend.services.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;

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

    @GetMapping("/refreshToken")
    public ResponseEntity<JwtRefreshResponse> refreshToken(HttpServletRequest request) throws NotFoundException, BadRequestException {
        JwtRefreshResponse userResponse = authenticationService.refreshToken(request);
        return ResponseEntity.ok(userResponse);
    }
    @PostMapping("/webLogin/{email}/{password}")
    public ResponseEntity<JwtAuthenticationResponse> userWebLogin(@PathVariable String email, @PathVariable String password)
            throws NotFoundException, BadRequestException {
        JwtAuthenticationResponse user = authenticationService.userWebLogin(email, password);
        return ResponseEntity.ok(user);
    }

    @PutMapping ("/update")
    public ResponseEntity<Void> updateUser(@RequestBody User userToUpdate) throws NotFoundException {
        userService.updateUser(userToUpdate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping ("/verifyEmail/{token}")
    public ResponseEntity<String> verifyEmail(@PathVariable String token) throws BadRequestException {
        String successMessage = userService.verifyEmail(token);
        return ResponseEntity.ok(successMessage);
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
    public void editPassword(@PathVariable Long userId, @PathVariable String oldPassword, @PathVariable String newPassword) throws BadRequestException {
        System.out.println("edit pass");
        userService.editPassword(userId, oldPassword, newPassword);
    }

    @PutMapping ("/uploadNewProfilePic")
    public ResponseEntity<User> uploadNewProfilePic(@RequestBody User user) throws NotFoundException {
        User newUser = userService.uploadNewProfilePic(user.getUser_id(), user.getProfile_pic());
        return ResponseEntity.ok(newUser);
    }

    // only for admin portal, not vendor portal
    @PutMapping("/toggleBlock/{userId}")
    @PreAuthorize("hasRole('INTERNAL_STAFF')")
    public void toggleBlock(@PathVariable Long userId) throws NotFoundException, ToggleBlockException {
        userService.toggleBlock(userId);
    }

    @PostMapping ("/webPasswordResetStageThree/{email}/{password}")
    public ResponseEntity<String> webPasswordResetStageThree(@PathVariable String email, @PathVariable String password)
            throws BadRequestException {
        String successMessage = userService.webPasswordResetStageThree(email, password);
        return ResponseEntity.ok(successMessage);
    }
    @GetMapping("/viewUserProfile/{userId}")
    public ResponseEntity<User> viewUserProfile(@PathVariable Long userId) throws NotFoundException {
        User user = userService.viewUserProfile(userId);
        return ResponseEntity.ok(user);
    }
}