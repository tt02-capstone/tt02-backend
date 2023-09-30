package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocalRepository extends JpaRepository<Local, Long> {
    @Query("SELECT l FROM Local l WHERE l.user_id=?1")
    Local getLocalByUserId(Long userId);
    
    @Query("SELECT ls FROM Local ls WHERE ls.email=?1")
    Local retrieveLocalByEmail(String email);

    @Query("SELECT ls FROM Local ls WHERE ls.password_reset_token=?1")
    Local retrieveLocalByToken(String token);

    @Query("SELECT l.user_id FROM Local l WHERE l.email = ?1")
    Long getLocalIdByEmail(String email);

    @Query("SELECT l.user_id FROM Local l WHERE l.nric_num = ?1")
    Long getLocalIdByNRICNum(String nric);

    @Query("SELECT l.user_id FROM Local l WHERE l.mobile_num = ?1")
    Long getLocalIdByMobileNum(String mobileNum);

    @Query("SELECT ls.stripe_account_id FROM Local ls WHERE ls.email=?1")
    String getStripeIdByEmail(String email);

    @Query("SELECT l FROM Local l JOIN l.tour_type_list t WHERE t.tour_type_id=?1")
    Local getLocalTiedToTourType(Long tourTypeId);

    @Query("SELECT l FROM Local l " +
            "JOIN l.tourTypes tt " +
            "JOIN tt.tourList t " +
            "WHERE t = :tour")
    Local findLocalByTour(@Param("tour") Tour tour);
}
