package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.DIYEvent;
import com.nus.tt02backend.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>{

   }


