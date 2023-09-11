package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.repositories.TouristRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TouristService {
    @Autowired
    TouristRepository touristRepository;
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

        Long existingId = touristRepository.getTouristIdByEmail(touristToCreate.getEmail());
        if (existingId != null && existingId != touristToCreate.getUser_id()) { // but there is an existing email
            throw new BadRequestException("Email currently in use. Please use a different email!");
        }

        existingId = touristRepository.getTouristIdByPassportNum(touristToCreate.getPassport_num());
        if (existingId != null && existingId != touristToCreate.getUser_id()) { // but there is a passport number
            throw new BadRequestException("Passport number currently in use. Please use a different passport number!");
        }

        existingId = touristRepository.getTouristIdByMobileNum(touristToCreate.getMobile_num());
        if (existingId != null && existingId != touristToCreate.getUser_id()) { // but there is an existing mobile number
            throw new BadRequestException("Mobile number currently in use. Please use a different mobile number!");
        }

        touristToCreate.setPassword(encoder.encode(touristToCreate.getPassword()));
        touristRepository.save(touristToCreate);
        return touristToCreate.getUser_id();
    }

    public List<Tourist> retrieveAllTourist() {
        return touristRepository.findAll();
    }

    public Tourist getTouristProfile(Long touristId) throws TouristNotFoundException {
        try {
            Tourist tourist = touristRepository.findById(touristId).get();

            if (tourist == null) {
                throw new TouristNotFoundException("Tourist not found!");
            }

            tourist.setPassword(null);

            return tourist;
        } catch(Exception ex) {
            throw new TouristNotFoundException(ex.getMessage());
        }
    }

    public Tourist editTouristProfile(Tourist touristToEdit) throws EditUserException {
        try {

            Optional<Tourist> touristOptional = touristRepository.findById(touristToEdit.getUser_id());

            if (touristOptional.isPresent()) {
                Tourist tourist = touristOptional.get();

                Long existingId = touristRepository.getTouristIdByEmail(touristToEdit.getEmail());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is an existing email
                    throw new EditUserException("Email currently in use. Please use a different email!");
                }

                existingId = touristRepository.getTouristIdByPassportNum(touristToEdit.getPassport_num());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is a passport number
                    throw new EditUserException("Passport number currently in use. Please use a different passport number!");
                }

                existingId = touristRepository.getTouristIdByMobileNum(touristToEdit.getMobile_num());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is an existing mobile number
                    throw new EditUserException("Mobile number currently in use. Please use a different mobile number!");
                }

                tourist.setEmail(touristToEdit.getEmail());
                tourist.setName(touristToEdit.getName());
                tourist.setDate_of_birth(touristToEdit.getDate_of_birth());
                tourist.setCountry_code(touristToEdit.getCountry_code());
                tourist.setMobile_num(tourist.getMobile_num());
                touristRepository.save(tourist);
                tourist.setPassword(null);
                return tourist;

            } else {
                throw new EditUserException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public void editTouristPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<Tourist> touristOptional = touristRepository.findById(id);

            if (touristOptional.isPresent()) {
                Tourist tourist = touristOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, tourist.getPassword())) {
                    tourist.setPassword(encoder.encode(newPassword));
                    touristRepository.save(tourist);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}
