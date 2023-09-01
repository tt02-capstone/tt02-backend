package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.repositories.StaffRepository;
import com.nus.tt02backend.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    StaffRepository staffRepository;
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
        InternalStaff internalStaff = staffRepository.findById((internalStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("InternalStaff not found"));

        if (internalStaff.getEmail().equals(internalStaffToUpdate.getEmail())) {
            if (internalStaffToUpdate.getUser_id() != null && !internalStaffToUpdate.getName().isEmpty()) {
                internalStaff.setName(internalStaffToUpdate.getName());
            }
        }

        staffRepository.save(internalStaff);
    }

    public Long createStaff(InternalStaff internalStaffToCreate) throws BadRequestException {
        List<InternalStaff> internalStaffs = retrieveAllStaff();

        for (InternalStaff internalStaff : internalStaffs) {
            if (internalStaff.getEmail().equals(internalStaffToCreate.getEmail())) {
                throw new BadRequestException("InternalStaff email exists");
            }
        }

        internalStaffToCreate.setPassword(encoder.encode(internalStaffToCreate.getPassword()));
        staffRepository.save(internalStaffToCreate);
        return internalStaffToCreate.getUser_id();
    }

    public List<InternalStaff> retrieveAllStaff() {
        return staffRepository.findAll();
    }
}
