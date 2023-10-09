package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.models.Post;
import com.nus.tt02backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/post")
public class PostController {
    @Autowired
    PostService postService;

    @PostMapping("/createPost/{userId}/{categoryItemId}")
    public ResponseEntity<Post> createPost(@PathVariable Long userId, @PathVariable Long categoryItemId,
                                           @RequestBody Post postToCreate)
            throws BadRequestException {
        Post post = postService.createPost(userId, categoryItemId, postToCreate);
        return ResponseEntity.ok(post);
    }

    @PutMapping("/updatePost")
    public ResponseEntity<Post> updatePost(@RequestBody Post postToUpdate) throws BadRequestException {
        Post post = postService.updatePost(postToUpdate);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/deletePost/{postIdToDelete}")
    public ResponseEntity<String> deletePost(@PathVariable Long postIdToDelete) throws BadRequestException {
        String responseMessage = postService.deletePost(postIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getAllPostByCategoryItemId/{categoryItemId}")
    public ResponseEntity<List<Post>> getAllPostByCategoryItemId(@PathVariable Long categoryItemId) throws NotFoundException {
        List<Post> list = postService.getAllPostByCategoryItemId(categoryItemId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getPost/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) throws NotFoundException {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/upvote/{userId}/{postId}")
    public ResponseEntity<Post> upvote(@PathVariable Long userId, @PathVariable Long postId) throws NotFoundException {
        Post post = postService.upvote(userId, postId);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/downvote/{userId}/{postId}")
    public ResponseEntity<Post> downvote(@PathVariable Long userId, @PathVariable Long postId) throws NotFoundException {
        Post post = postService.downvote(userId, postId);
        return ResponseEntity.ok(post);
    }
}
