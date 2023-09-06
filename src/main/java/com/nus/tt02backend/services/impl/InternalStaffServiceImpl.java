package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.services.InternalStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternalStaffServiceImpl implements InternalStaffService {
    @Autowired
    InternalStaffRepository internalStaffRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public InternalStaff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, internalStaff.getPassword())
                && !internalStaff.getIs_blocked()) {
            internalStaff.setComment_list(null);
            internalStaff.setPost_list(null);
            internalStaff.setBadge_list(null);
            internalStaff.setSupport_ticket_list(null);
            return internalStaff;
        } else if (internalStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator");
        }
        else {
            throw new BadRequestException("Incorrect password");
        }
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
}
