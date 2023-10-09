package com.nus.tt02backend.controllers;

import com.nus.tt02backend.services.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/badge")
public class BadgeController {
    @Autowired
    BadgeService badgeService;

}
