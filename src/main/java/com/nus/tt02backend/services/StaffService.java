package com.nus.tt02backend.services;

import com.nus.tt02backend.models.Staff;

public interface StaffService {
    public Staff staffLogin(String email, String password);
    public void updateStaff(Staff staffToUpdate);
    public Long createStaff(Staff staffToCreate);
}
