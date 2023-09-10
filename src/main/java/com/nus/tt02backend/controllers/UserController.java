package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.exceptions.ToggleBlockException;
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

    @PutMapping("/toggleBlock/{userId}")
    public void vendorStaffLogin(@PathVariable Long userId) throws NotFoundException, ToggleBlockException {
        userService.toggleBlock(userId);
    }
}
