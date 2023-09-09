package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.services.UserService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InternalStaffRepository extends JpaRepository<InternalStaff, Long> {

    @Query("SELECT ins FROM InternalStaff ins WHERE ins.email=?1")
    InternalStaff getInternalStaffByEmail(String email);

    @Query("SELECT ins FROM InternalStaff ins WHERE ins.password_reset_token=?1")
    InternalStaff getInternalStaffByToken(String token);

    @Query("SELECT u.user_id FROM User u WHERE u.email = ?1")
    Long getAdminByEmail(String email);

    @Query("SELECT MAX(s.staff_num) FROM InternalStaff s")
    Long getLatestStaffNum();

}
