package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TouristService {
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    UserRepository userRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Tourist touristLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<Tourist> tourists = retrieveAllTourist();

        for (Tourist tourist : tourists) {
            if (tourist.getEmail().equals(email)) {
                if (encoder.matches(password, tourist.getPassword())) {
                    return tourist;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("Tourist account not found");
    }

    public void updateTourist(Tourist touristToUpdate) throws NotFoundException {
        Tourist tourist = touristRepository.findById((touristToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("Rourist not found"));

        if (tourist.getEmail().equals(touristToUpdate.getEmail())) {
            if (touristToUpdate.getName() != null && !touristToUpdate.getName().isEmpty()) {
                tourist.setName(touristToUpdate.getName());
            }
        }

        touristRepository.save(tourist);
    }

    public Long createTourist(Tourist touristToCreate) throws BadRequestException {

        Long existingId = userRepository.getUserIdByEmail(touristToCreate.getEmail()); // need to check with all users

        if (existingId != null) {
            throw new BadRequestException("Tourist email exists!");
        }

        touristToCreate.setPassword(encoder.encode(touristToCreate.getPassword()));
        touristRepository.save(touristToCreate);
        return touristToCreate.getUser_id();
    }

    public List<Tourist> retrieveAllTourist() {
        return touristRepository.findAll();
    }
}
