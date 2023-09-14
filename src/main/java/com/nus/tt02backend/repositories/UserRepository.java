package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email=?1 and (u.user_type = 'TOURIST' or u.user_type = 'LOCAL')")
    User retrieveTouristOrLocalByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email=?1 and (u.user_type = 'VENDOR_STAFF' or u.user_type = 'LOCAL')")
    User retrieveVendorStaffOrLocalByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.password_reset_token=?1 and (u.user_type = 'TOURIST' or u.user_type = 'LOCAL')")
    User retrieveTouristOrLocalByToken(String token);

    @Query("SELECT u FROM User u WHERE u.password_reset_token=?1 and (u.user_type = 'VENDOR_STAFF' or u.user_type = 'LOCAL')")
    User retrieveVendorStaffOrLocalByToken(String token);
}