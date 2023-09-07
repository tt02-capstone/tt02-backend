package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VendorService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public VendorStaff vendorLogin(String email, String password) throws NotFoundException, BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

        if (vendorStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, vendorStaff.getPassword())
                && !vendorStaff.getIs_blocked()
                && vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.APPROVED) {
            vendorStaff.getVendor().setWithdrawal_list(null);
            vendorStaff.getVendor().setVendor_staff_list(null);
            vendorStaff.getVendor().setComment_list(null);
            vendorStaff.getVendor().setPost_list(null);
            vendorStaff.getVendor().setAttraction_list(null);
            vendorStaff.getVendor().setAccommodation_list(null);
            vendorStaff.getVendor().setRestaurant_list(null);
            vendorStaff.getVendor().setTelecom_list(null);
            vendorStaff.getVendor().setDeals_list(null);
            return vendorStaff;
        } else if (vendorStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator");
        }
        else if (vendorStaff.getVendor().getApplication_status() != ApplicationStatusEnum.APPROVED) {
            throw new BadRequestException("Your application is still pending review");
        }
        else {
            throw new BadRequestException("Incorrect password");
        }
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
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(vendorStaffToCreate.getEmail());

        if (vendorStaff != null) {
            throw new BadRequestException("The email address has been used, please enter another email");
        }

        Vendor vendorToCreate = vendorStaffToCreate.getVendor();
        vendorRepository.save(vendorToCreate);

        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        vendorStaffRepository.save(vendorStaffToCreate);

        return vendorStaffToCreate.getUser_id();
    }

    public List<VendorStaff> retrieveAllVendors() {
        return vendorStaffRepository.findAll();
    }
}
