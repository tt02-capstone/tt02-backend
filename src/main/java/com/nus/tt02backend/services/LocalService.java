package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocalService {

    @Autowired
    LocalRepository localRepository;
    @Autowired
    UserRepository userRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Long createLocal(Local localToCreate) throws CreateLocalException {
        try {
            Long existingId = localRepository.getLocalIdByEmail(localToCreate.getEmail());
            if (existingId != null && existingId != localToCreate.getUser_id()) { // but there is an existing email
                throw new CreateLocalException("Email currently in use. Please use a different email!");
            }

            existingId = localRepository.getLocalIdByNRICNum(localToCreate.getNric_num());
            if (existingId != null && existingId != localToCreate.getUser_id()) { // but there is a NRIC number
                throw new CreateLocalException("NRIC number currently in use. Please use a different NRIC number!");
            }

            existingId = localRepository.getLocalIdByMobileNum(localToCreate.getMobile_num());
            if (existingId != null && existingId != localToCreate.getUser_id()) { // but there is a mobile number
                throw new CreateLocalException("Mobile number currently in use. Please use a different mobile number!");
            }

            localToCreate.setPassword(encoder.encode(localToCreate.getPassword()));
            localRepository.save(localToCreate);
            return localToCreate.getUser_id();
        } catch (Exception ex) {
            throw new CreateLocalException(ex.getMessage());
        }
    }

    public Local getLocalProfile(Long localId) throws LocalNotFoundException {
        try {
            Local local = localRepository.findById(localId).get();

            if (local == null) {
                throw new LocalNotFoundException("Local not found!");
            }

            local.setPassword(null);

            return local;
        } catch(Exception ex) {
            throw new LocalNotFoundException(ex.getMessage());
        }
    }

    public Local editLocalProfile(Local localToEdit) throws EditUserException {
        try {

            Optional<Local> localOptional = localRepository.findById(localToEdit.getUser_id());

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                Long existingId = localRepository.getLocalIdByEmail(localToEdit.getEmail());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is an existing email
                    throw new EditUserException("Email currently in use. Please use a different email!");
                }

                existingId = localRepository.getLocalIdByNRICNum(local.getNric_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a NRIC number
                    throw new EditUserException("NRIC number currently in use. Please use a different NRIC number!");
                }

                existingId = localRepository.getLocalIdByMobileNum(localToEdit.getMobile_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a mobile number
                    throw new CreateLocalException("Mobile number currently in use. Please use a different mobile number!");
                }

                local.setEmail(localToEdit.getEmail());
                local.setName(localToEdit.getName());
                local.setDate_of_birth(localToEdit.getDate_of_birth());
                local.setCountry_code(localToEdit.getCountry_code());
                local.setMobile_num(localToEdit.getMobile_num());
                localRepository.save(local);
                local.setPassword(null);
                return local;

            } else {
                throw new EditUserException("Local not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public void editLocalPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<Local> localOptional = localRepository.findById(id);

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, local.getPassword())) {
                    local.setPassword(encoder.encode(newPassword));
                    localRepository.save(local);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("Local not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}