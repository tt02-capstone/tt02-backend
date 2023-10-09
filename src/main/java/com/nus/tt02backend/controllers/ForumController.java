package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Post;
import com.nus.tt02backend.services.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    ForumService forumService;

    @PostMapping("createPost/{userId}/{categoryItemId}")
    public ResponseEntity<Post> createPost(@PathVariable Long userId, @PathVariable Long categoryItemId,
                                           @RequestBody Post postToCreate)
            throws BadRequestException {
        Post post = forumService.createPost(userId, categoryItemId, postToCreate);
        return ResponseEntity.ok(post);
    }
}
