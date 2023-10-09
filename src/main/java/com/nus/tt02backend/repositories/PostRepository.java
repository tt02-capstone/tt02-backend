package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>{

}


