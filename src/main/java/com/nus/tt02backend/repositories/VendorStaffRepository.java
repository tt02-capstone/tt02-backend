package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VendorStaffRepository extends JpaRepository<VendorStaff, Long> {
    @Query("SELECT vs FROM VendorStaff vs WHERE vs.email=?1")
    VendorStaff retrieveVendorStaffByEmail(String email);

    @Query("SELECT vs FROM VendorStaff vs WHERE vs.password_reset_token=?1")
    VendorStaff retrieveVendorStaffByToken(String token);
}
