package com.nus.tt02backend.services;

import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.ReportRepository;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BadgeService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    BadgeService badgeService;

}