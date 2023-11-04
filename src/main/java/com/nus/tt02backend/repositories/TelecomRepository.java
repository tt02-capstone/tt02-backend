package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Telecom;
import com.nus.tt02backend.models.enums.NumberOfValidDaysEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.*;

public interface TelecomRepository extends JpaRepository<Telecom, Long>{

    @Query("SELECT t FROM Telecom t WHERE t.is_published = true")
    List<Telecom> getPublishedTelecomList();

    @Query("SELECT t FROM Telecom t WHERE t.plan_duration_category=?1 AND t.is_published=true")
    List<Telecom> getTelecomBasedOnDays(NumberOfValidDaysEnum plan_duration_category);

    @Query("SELECT t.name FROM Telecom t")
    List<String> getTelecomNameList();

    @Query("SELECT t FROM Telecom t where t.name=?1")
    List<Telecom> getTelecomListbyName(String name);
}


