package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    @Query("SELECT v FROM Vendor v WHERE v.application_status=?1")
    List<Vendor> retrievePendingVendorApplications(ApplicationStatusEnum applicationStatus);

    @Query("SELECT v.vendor_id FROM Vendor v")
    List<Long> getAllVendorId();

    @Query("SELECT v.stripe_account_id FROM Vendor v WHERE v.business_name=?1")
    String getStripeIdByName(String business_name);

    @Query("SELECT v FROM Vendor v JOIN v.attraction_list a WHERE a.name = :attractionName")
    Vendor findVendorByAttractionName(@Param("attractionName") String attractionName);

    @Query("SELECT v FROM Vendor v JOIN v.telecom_list a WHERE a.name = :telecomName")
    Vendor findVendorByTelecomName(@Param("telecomName") String telecomName);

    @Query("SELECT v FROM Vendor v JOIN v.telecom_list a WHERE a.name = :telecomName")
    Optional<Vendor> findVendorOptionalByTelecomName(@Param("telecomName") String telecomName);
    @Query("SELECT v FROM Vendor v JOIN v.telecom_list a WHERE a.telecom_id = :telecomId")
    Vendor findVendorByTelecomId(@Param("telecomId") Long telecomId);

    @Query("SELECT v FROM Vendor v JOIN v.item_list a WHERE a.item_id = :itemId")
    Vendor findVendorByItemId(@Param("itemId") Long itemId);

    @Query("SELECT v FROM Vendor v JOIN v.accommodation_list a JOIN a.room_list r WHERE r.room_id  = :roomId")
    Vendor findVendorByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT v FROM Vendor v JOIN v.accommodation_list a WHERE a.name = :accommodationName")
    Vendor findVendorByAccommodationName(@Param("accommodationName") String accommodationName);

    @Query("SELECT v FROM Vendor v WHERE v.business_name = ?1")
    Vendor findVendorByBusinessName(String businessName);

    @Query("SELECT v FROM Vendor v WHERE v.stripe_account_id = ?1")
    Vendor findByStripeId(String stripeId);
}
