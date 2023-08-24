package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exception.BadRequestException;
import com.nus.tt02backend.exception.NotFoundException;
import com.nus.tt02backend.models.Staff;
import com.nus.tt02backend.repositories.StaffRepository;
import com.nus.tt02backend.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffServiceImpl implements StaffService {
    @Autowired
    StaffRepository staffRepository;

    public Staff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<Staff> staffs = retrieveAllStaff();

        for (Staff staff : staffs) {
            if (staff.getEmail().equals(email)) {
                if (staff.getPassword().equals(password)) {
                    return staff;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("Staff account not found");
    }

    public void updateStaff(Staff staffToUpdate) throws NotFoundException {
        Staff staff = staffRepository.findById((staffToUpdate.getStaffId()))
                .orElseThrow(() -> new NotFoundException("Staff not found"));

        if (staff.getEmail().equals(staffToUpdate.getEmail())) {
            if (staffToUpdate.getStaffName() != null && !staffToUpdate.getStaffName().isEmpty()) {
                staff.setStaffName(staffToUpdate.getStaffName());
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

        staffRepository.save(staffToCreate);
        return staffToCreate.getStaffId();
    }

    public List<Staff> retrieveAllStaff() {
        return staffRepository.findAll();
    }
}
