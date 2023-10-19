package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.*;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
