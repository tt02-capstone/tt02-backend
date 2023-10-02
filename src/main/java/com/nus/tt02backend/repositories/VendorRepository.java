package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    @Query("SELECT v FROM Vendor v WHERE v.application_status=?1")
    List<Vendor> retrievePendingVendorApplications(ApplicationStatusEnum applicationStatus);

    @Query("SELECT v.stripe_account_id FROM Vendor v WHERE v.business_name=?1")
    String getStripeIdByName(String business_name);

    @Query("SELECT v FROM Vendor v JOIN v.attraction_list a WHERE a.name = :attractionName")
    Vendor findVendorByAttractionName(@Param("attractionName") String attractionName);

    @Query("SELECT v FROM Vendor v JOIN v.telecom_list a WHERE a.name = :telecomName")
    Vendor findVendorByTelecomName(@Param("telecomName") String telecomName);

    @Query("SELECT v FROM Vendor v JOIN v.accommodation_list a WHERE a.name = :accommodationName")
    Vendor findVendorByAccommodationName(@Param("accommodationName") String accommodationName);

    @Query("SELECT v FROM Vendor v WHERE v.business_name = ?1")
    Vendor findVendorByBusinessName(String businessName);
}
