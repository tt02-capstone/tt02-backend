package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.exceptions.TouristNotFoundException;
import com.nus.tt02backend.exceptions.VendorNotFoundException;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.repositories.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {
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
        VendorStaff vendorStaff = vendorRepository.findById((vendorStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));

        if (vendorStaff.getEmail().equals(vendorStaffToUpdate.getEmail())) {
            if (vendorStaffToUpdate.getName() != null && !vendorStaffToUpdate.getName().isEmpty()) {
                vendorStaff.setName(vendorStaffToUpdate.getName());
            }
        }

        vendorRepository.save(vendorStaff);
    }

    public Long createVendor(VendorStaff vendorStaffToCreate) throws BadRequestException {
        List<VendorStaff> vendorStaffs = retrieveAllVendors();

        for (VendorStaff vendorStaff : vendorStaffs) {
            if (vendorStaff.getEmail().equals(vendorStaffToCreate.getEmail())) {
                throw new BadRequestException("VendorStaff email exists");
            }
        }

        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        vendorRepository.save(vendorStaffToCreate);
        return vendorStaffToCreate.getUser_id();
    }

    public List<VendorStaff> retrieveAllVendors() {
        return vendorRepository.findAll();
    }

    public VendorStaff retrieveVendorProfile(Long vendorId) throws IllegalArgumentException, VendorNotFoundException {
        try {
            VendorStaff vendor = vendorRepository.findById(vendorId).get();

            if (vendor == null) {
                throw new VendorNotFoundException("Vendor not found!");
            }

            vendor.setPassword(null);

            return vendor;
        } catch(Exception ex) {
            throw new VendorNotFoundException(ex.getMessage());
        }
    }
}
