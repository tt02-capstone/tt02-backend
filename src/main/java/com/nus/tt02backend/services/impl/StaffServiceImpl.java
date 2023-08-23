package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.models.Staff;
import com.nus.tt02backend.repositories.StaffRepository;
import com.nus.tt02backend.services.StaffService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    StaffRepository staffRepository;

    public Staff staffLogin(String email, String password) {
        List<Staff> staffs = retrieveAllStaff();

        for (Staff staff : staffs) {
            if (staff.getEmail().equals(email)) {
                if (staff.getPassword().equals(password)) {
                    return staff;
                }
            }
        }

        throw new EntityNotFoundException("Invalid credentials");
    }

    public void updateStaff(Staff staffToUpdate) {
        Staff staff = staffRepository.findById((staffToUpdate.getStaffId()))
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (staff.getEmail().equals(staffToUpdate.getEmail())) {
            if (staffToUpdate.getStaffName() != null && !staffToUpdate.getStaffName().isEmpty()) {
                staff.setStaffName(staffToUpdate.getStaffName());
            }
        }

        staffRepository.save(staff);
    }

    public Long createStaff(Staff staffToCreate) {
        List<Staff> staffs = retrieveAllStaff();

        for (Staff staff : staffs) {
            if (staff.getEmail().equals(staffToCreate.getEmail())) {
                throw new IllegalArgumentException("Staff email exists!");
            }
        }

        staffRepository.save(staffToCreate);
        return staffToCreate.getStaffId();
    }

    public List<Staff> retrieveAllStaff() {
        return staffRepository.findAll();
    }
}
