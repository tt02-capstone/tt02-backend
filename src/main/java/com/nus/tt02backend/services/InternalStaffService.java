package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.AdminNotFoundException;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.EditAdminException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import org.hibernate.Internal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InternalStaffService {
    @Autowired
    InternalStaffRepository internalStaffRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public InternalStaff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<InternalStaff> internalStaffs = retrieveAllStaff();

        for (InternalStaff internalStaff : internalStaffs) {
            if (internalStaff.getEmail().equals(email)) {
                if (encoder.matches(password, internalStaff.getPassword())) {
                    return internalStaff;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("InternalStaff account not found");
    }

    public void updateStaff(InternalStaff internalStaffToUpdate) throws NotFoundException {
        InternalStaff internalStaff = internalStaffRepository.findById((internalStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("InternalStaff not found"));

        if (internalStaff.getEmail().equals(internalStaffToUpdate.getEmail())) {
            if (internalStaffToUpdate.getUser_id() != null && !internalStaffToUpdate.getName().isEmpty()) {
                internalStaff.setName(internalStaffToUpdate.getName());
            }
        }

        internalStaffRepository.save(internalStaff);
    }

    public Long createStaff(InternalStaff internalStaffToCreate) throws BadRequestException {
        List<InternalStaff> internalStaffs = retrieveAllStaff();

        for (InternalStaff internalStaff : internalStaffs) {
            if (internalStaff.getEmail().equals(internalStaffToCreate.getEmail())) {
                throw new BadRequestException("InternalStaff email exists");
            }
        }

        internalStaffToCreate.setPassword(encoder.encode(internalStaffToCreate.getPassword()));
        internalStaffRepository.save(internalStaffToCreate);
        return internalStaffToCreate.getUser_id();
    }

    public List<InternalStaff> retrieveAllStaff() {
        return internalStaffRepository.findAll();
    }

    public InternalStaff getStaffProfile(Long staffId) throws IllegalArgumentException, AdminNotFoundException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffId);

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();
                internalStaff.setPassword(null);
                return internalStaff;

            } else {
                 throw new AdminNotFoundException("Admin staff not found!");
            }

        } catch(Exception ex) {
            throw new AdminNotFoundException(ex.getMessage());
        }
    }

    public InternalStaff editStaffProfile(InternalStaff staffToEdit) throws EditAdminException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffToEdit.getUser_id());

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();
                internalStaff.setEmail(staffToEdit.getEmail());
                internalStaff.setName(staffToEdit.getName());
                internalStaffRepository.save(internalStaff);
                internalStaff.setPassword(null);
                return internalStaff;

            } else {
                throw new EditAdminException("Admin staff not found!");
            }
        } catch (Exception ex) {
            throw new EditAdminException(ex.getMessage());
        }
    }

    public void editStaffPassword(Long id, String oldPassword, String newPassword) throws EditAdminException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(id);

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();
                if (encoder.matches(oldPassword, internalStaff.getPassword())) {
                    internalStaff.setPassword(encoder.encode(newPassword));
                    internalStaffRepository.save(internalStaff);
                } else {
                    throw new BadRequestException("Incorrect old password!");
                }

            } else {
                throw new EditAdminException("Admin staff not found!");
            }
        } catch (Exception ex) {
            throw new EditAdminException(ex.getMessage());
        }
    }
}
