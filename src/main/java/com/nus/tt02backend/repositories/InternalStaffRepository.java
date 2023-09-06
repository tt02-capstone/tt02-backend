package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InternalStaffRepository extends JpaRepository<InternalStaff, Long> {
    @Query("SELECT vs FROM InternalStaff vs WHERE vs.email=?1")
    InternalStaff retrieveInternalStaffByEmail(String email);
}
