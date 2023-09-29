package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.TicketPerDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TicketPerDayRepository extends JpaRepository<TicketPerDay, Long> {
}
