package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import com.nus.tt02backend.services.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorServiceImpl implements VendorService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public VendorStaff vendorLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<VendorStaff> vendorStaffs = retrieveAllVendors();

        for (VendorStaff vendorStaff : vendorStaffs) {
            if (vendorStaff.getEmail().equals(email)) {
                if (encoder.matches(password, vendorStaff.getPassword())) {
                    return vendorStaff;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("VendorStaff account not found");
    }

    public void updateVendor(VendorStaff vendorStaffToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById((vendorStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));

        if (vendorStaff.getEmail().equals(vendorStaffToUpdate.getEmail())) {
            if (vendorStaffToUpdate.getName() != null && !vendorStaffToUpdate.getName().isEmpty()) {
                vendorStaff.setName(vendorStaffToUpdate.getName());
            }
        }

        vendorStaffRepository.save(vendorStaff);
    }

    public Long createVendor(VendorStaff vendorStaffToCreate) throws BadRequestException {
        List<VendorStaff> vendorStaffs = retrieveAllVendors();
        for (VendorStaff vendorStaff : vendorStaffs) {
            if (vendorStaff.getEmail().equals(vendorStaffToCreate.getEmail())) {
                throw new BadRequestException("The email address has been used, please enter another email");
            }
        }

        Vendor vendorToCreate = vendorStaffToCreate.getVendor();
        vendorRepository.save(vendorToCreate);

        //vendorStaffToCreate.setVendor(vendorToCreate);
        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        vendorStaffRepository.save(vendorStaffToCreate);

        return vendorStaffToCreate.getUser_id();
    }

    public List<VendorStaff> retrieveAllVendors() {
        return vendorStaffRepository.findAll();
    }
}
