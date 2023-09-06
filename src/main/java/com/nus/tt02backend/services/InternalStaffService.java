package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.InternalStaff;

public interface InternalStaffService {
    public InternalStaff staffLogin(String email, String password) throws NotFoundException, BadRequestException;
    public void updateStaff(InternalStaff internalStaffToUpdate) throws NotFoundException;
    public Long createStaff(InternalStaff internalStaffToCreate) throws BadRequestException;
}
