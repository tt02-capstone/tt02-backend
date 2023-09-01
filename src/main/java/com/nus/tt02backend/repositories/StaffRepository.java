package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.InternalStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<InternalStaff, Long> {
}
