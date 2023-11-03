package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InternalStaffRepository extends JpaRepository<InternalStaff, Long> {

    @Query("SELECT ins FROM InternalStaff ins WHERE ins.email=?1")
    InternalStaff retrieveInternalStaffByEmail(String email);

    @Query("SELECT u.user_id FROM User u WHERE u.email = ?1")
    Long getAdminByEmail(String email);

    @Query("SELECT MAX(s.staff_num) FROM InternalStaff s")
    Long getLatestStaffNum();

    @Query("SELECT ins FROM InternalStaff ins WHERE ins.password_reset_token=?1")
    InternalStaff retrieveInternalStaffByToken(String token);

    @Query("SELECT i FROM InternalStaff i WHERE i.user_id=?1")
    InternalStaff getInternalStaffByUserId(Long userId);
}
