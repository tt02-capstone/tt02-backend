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
}
