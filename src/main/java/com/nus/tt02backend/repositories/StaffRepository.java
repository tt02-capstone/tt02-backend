package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Long> {
}
