package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.SupportTicket;
import com.nus.tt02backend.models.Telecom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.*;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

}
