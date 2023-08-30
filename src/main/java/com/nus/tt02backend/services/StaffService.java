package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Staff;

public interface StaffService {
    public Staff staffLogin(String email, String password) throws NotFoundException, BadRequestException;
    public void updateStaff(Staff staffToUpdate) throws NotFoundException;
    public Long createStaff(Staff staffToCreate) throws BadRequestException;
}
