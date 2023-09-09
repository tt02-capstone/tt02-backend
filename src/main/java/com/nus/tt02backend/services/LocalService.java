package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalService {

    @Autowired
    LocalRepository localRepository;
    @Autowired
    UserRepository userRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Long createLocal(Local localToCreate) throws BadRequestException {

        Long existingId = userRepository.getUserIdByEmail(localToCreate.getEmail()); // need to check with all users

        if (existingId != null) {
            throw new BadRequestException("Local email exists!");
        }

        localToCreate.setPassword(encoder.encode(localToCreate.getPassword()));
        localRepository.save(localToCreate);
        return localToCreate.getUser_id();
    }
}
