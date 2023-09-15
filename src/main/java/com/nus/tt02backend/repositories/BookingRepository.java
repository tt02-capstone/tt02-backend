package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long>  {
}
