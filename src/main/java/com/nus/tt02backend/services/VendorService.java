package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;

public interface VendorService {
    public Vendor vendorLogin(String email, String password) throws NotFoundException, BadRequestException;
    public void updateVendor(Vendor vendorToUpdate) throws NotFoundException;
    public Long createVendor(Vendor vendorToCreate) throws BadRequestException;
}
