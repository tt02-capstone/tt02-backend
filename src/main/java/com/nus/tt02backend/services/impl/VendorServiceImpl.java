package com.nus.tt02backend.services.impl;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.services.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorServiceImpl implements VendorService {
    @Autowired
    VendorRepository vendorRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Vendor vendorLogin(String email, String password) throws NotFoundException, BadRequestException {
        List<Vendor> vendors = retrieveAllVendors();

        for (Vendor vendor : vendors) {
            if (vendor.getEmail().equals(email)) {
                if (encoder.matches(password, vendor.getPassword())) {
                    return vendor;
                } else {
                    throw new BadRequestException("Incorrect password");
                }
            }
        }

        throw new NotFoundException("Vendor account not found");
    }

    public void updateVendor(Vendor vendorToUpdate) throws NotFoundException {
        Vendor vendor = vendorRepository.findById((vendorToUpdate.getVendor_id()))
                .orElseThrow(() -> new NotFoundException("Vendor not found"));

        if (vendor.getEmail().equals(vendorToUpdate.getEmail())) {
            if (vendorToUpdate.getVendor_name() != null && !vendorToUpdate.getVendor_name().isEmpty()) {
                vendor.setVendor_name(vendorToUpdate.getVendor_name());
            }
        }

        vendorRepository.save(vendor);
    }

    public Long createVendor(Vendor vendorToCreate) throws BadRequestException {
        List<Vendor> vendors = retrieveAllVendors();

        for (Vendor vendor : vendors) {
            if (vendor.getEmail().equals(vendorToCreate.getEmail())) {
                throw new BadRequestException("Vendor email exists");
            }
        }

        vendorToCreate.setPassword(encoder.encode(vendorToCreate.getPassword()));
        vendorRepository.save(vendorToCreate);
        return vendorToCreate.getVendor_id();
    }

    public List<Vendor> retrieveAllVendors() {
        return vendorRepository.findAll();
    }
}
