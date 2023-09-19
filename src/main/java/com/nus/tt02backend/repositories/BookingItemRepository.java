package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.BookingItem;
import com.nus.tt02backend.models.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long>  {


}