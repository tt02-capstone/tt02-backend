package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

//    @PutMapping("/vendorPortalLogin/{email}/{password}")
//    public ResponseEntity<User> vendorPortalLogin(@PathVariable String email, @PathVariable String password)
//            throws NotFoundException, BadRequestException {
//        User user = userService.vendorPortalLogin(email, password);
//        return ResponseEntity.ok(user);
//    }
//
//    @GetMapping("/getUserProfile/{userId}")
//    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) throws UserNotFoundException {
//        User user = userService.getUserProfile(userId);
//        return ResponseEntity.ok(user);
//    }
//
//    @PutMapping("/editUserProfile")
//    public ResponseEntity<User> editUserProfile(@RequestBody User userToEdit) throws EditUserException {
//        User user = userService.editUserProfile(userToEdit);
//        return ResponseEntity.ok(user);
//    }
//
//    @PutMapping("/editUserPassword/{userId}/{oldPassword}/{newPassword}")
//    public void editUserPassword(@PathVariable Long userId, @PathVariable String oldPassword, @PathVariable String newPassword) throws EditPasswordException {
//        userService.editUserPassword(userId, oldPassword, newPassword);
//    }
}
