package com.nus.tt02backend.services;

import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    ReportRepository reportRepository;

}