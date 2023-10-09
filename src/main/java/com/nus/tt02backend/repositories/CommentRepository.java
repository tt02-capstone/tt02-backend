package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>{
}


