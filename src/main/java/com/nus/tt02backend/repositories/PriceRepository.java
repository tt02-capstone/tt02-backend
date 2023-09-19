package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface PriceRepository extends JpaRepository<Price, Long>{
}
