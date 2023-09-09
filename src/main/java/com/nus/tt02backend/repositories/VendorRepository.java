package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
}
