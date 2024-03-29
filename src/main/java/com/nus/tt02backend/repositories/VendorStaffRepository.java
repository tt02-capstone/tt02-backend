package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VendorStaffRepository extends JpaRepository<VendorStaff, Long> {
    @Query("SELECT vs FROM VendorStaff vs WHERE vs.email=?1")
    VendorStaff retrieveVendorStaffByEmail(String email);

    @Query("SELECT vs FROM VendorStaff vs WHERE vs.password_reset_token=?1")
    VendorStaff retrieveVendorStaffByPasswordToken(String token);

    @Query("SELECT vs FROM VendorStaff vs WHERE vs.email_verification_token=?1")
    VendorStaff retrieveVendorStaffByEmailToken(String token);

    @Query("SELECT u.user_id FROM User u WHERE u.email = ?1")
    Long getVendorStaffIdByEmail(String email);

    @Query("SELECT vs FROM VendorStaff vs WHERE vs.vendor.vendor_id = ?1")
    List<VendorStaff> getAllAssociatedVendorStaff(Long vendorId);
}
