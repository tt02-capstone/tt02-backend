package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Staff;
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

    public Staff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<Staff> staffs = retrieveAllStaff();

        for (Staff staff : staffs) {
            if (staff.getEmail().equals(email)) {
                if (encoder.matches(password, staff.getPassword())) {
                    return staff;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("Staff account not found");
    }

    public void updateStaff(Staff staffToUpdate) throws NotFoundException {
        Staff staff = staffRepository.findById((staffToUpdate.getStaff_id()))
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        if (staff.getEmail().equals(staffToUpdate.getEmail())) {
            if (staffToUpdate.getStaff_name() != null && !staffToUpdate.getStaff_name().isEmpty()) {
                staff.setStaff_name(staffToUpdate.getStaff_name());
            }
        }

        staffRepository.save(staff);
    }

    public Long createStaff(Staff staffToCreate) throws BadRequestException {
        List<Staff> staffs = retrieveAllStaff();

        for (Staff staff : staffs) {
            if (staff.getEmail().equals(staffToCreate.getEmail())) {
                throw new BadRequestException("Staff email exists");
            }
        }

        staffToCreate.setPassword(encoder.encode(staffToCreate.getPassword()));
        staffRepository.save(staffToCreate);
        return staffToCreate.getStaff_id();
    }

    public List<Staff> retrieveAllStaff() {
        return staffRepository.findAll();
    }
}
