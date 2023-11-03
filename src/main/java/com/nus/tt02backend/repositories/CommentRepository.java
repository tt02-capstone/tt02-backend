package com.nus.tt02backend.repositories;

import com.nus.tt02backend.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;


import java.util.*;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.parent_comment IS NULL AND c.post.post_id = ?1")
    List<Comment> getPostMasterComments(Long postId);
}


