package com.nus.tt02backend.controllers;

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
}
