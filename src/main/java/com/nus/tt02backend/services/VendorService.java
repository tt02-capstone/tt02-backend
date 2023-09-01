package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.VendorStaff;

public interface VendorService {
    public VendorStaff vendorLogin(String email, String password) throws NotFoundException, BadRequestException;
    public void updateVendor(VendorStaff vendorStaffToUpdate) throws NotFoundException;
    public Long createVendor(VendorStaff vendorStaffToCreate) throws BadRequestException;
}
