package com.nus.tt02backend.repositories;

import com.nus.tt02backend.dto.ItineraryFriendResponse;
import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email=?1 and (u.user_type = 'TOURIST' or u.user_type = 'LOCAL')")
    User retrieveTouristOrLocalByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email=?1 and (u.user_type = 'VENDOR_STAFF' or u.user_type = 'LOCAL')")
    User retrieveVendorStaffOrLocalByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.password_reset_token=?1 and (u.user_type = 'TOURIST' or u.user_type = 'LOCAL')")
    User retrieveTouristOrLocalByToken(String token);

    @Query("SELECT u FROM User u WHERE u.email_verification_token=?1")
    Tourist retrieveTouristByEmailVerificationToken(String email_verification_token);

    @Query("SELECT u.user_id FROM User u WHERE u.email = ?1")
    Long retrieveIdByUserEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> retrieveUserEmail(String email);

    @Query("SELECT u FROM User u WHERE u.password_reset_token=?1 and (u.user_type = 'VENDOR_STAFF' or u.user_type = 'LOCAL')")
    User retrieveVendorStaffOrLocalByToken(String token);

    @Query("SELECT u FROM User u WHERE u.user_id != ?1 AND (u.user_type = 'TOURIST' OR u.user_type = 'LOCAL') AND u.email LIKE CONCAT('%', ?2 ,'%')")
    List<User> getUserListWithEmailSimilarity(Long masterItineraryUserId, String email);

    @Query("SELECT u.profile_pic FROM User u WHERE u.user_id = ?1")
    String getProfileImageByIdList(Long userId);
}
