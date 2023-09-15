package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
