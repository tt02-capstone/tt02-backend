package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND p.booking.attraction.attraction_id = ?1")
    Double retrieveSumOfBookingByAttractionId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.telecom.telecom_id = ?1")
    Double retrieveSumOfBookingByTelecomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.room.room_id = ?1")
    Double retrieveSumOfBookingByRoomId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount), 0) FROM Payment p WHERE p.is_paid = true AND p.booking.status != 'CANCELLED' AND  p.booking.deal.deal_id = ?1")
    Double retrieveSumOfBookingByDealId(Long id);

    @Query("SELECT COALESCE(SUM(p.payment_amount),0) FROM Payment p WHERE p.is_paid = true  AND p.booking.type = 'TOUR'")
    Double retrieveTourEarningsByLocalId(Long localId);

    @Query("SELECT NEW java.util.ArrayList<>(b.start_datetime, SUM((p.payment_amount - p.comission_percentage) * p.payment_amount)) " +
            "FROM Payment p " +
            "INNER JOIN p.booking b " +
            "WHERE b.start_datetime >= :startDate " +
            "AND b.start_datetime <= :endDate " +
            "AND b.entityId = :entityId " +
            "AND b.entityType = :entityType " +
            "GROUP BY b.start_datetime " +
            "ORDER BY b.start_datetime")
    List<Object[]> getRevenueOverTime(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("entityId") Long entityId,
                                                           @Param("entityType") String entityType);
}