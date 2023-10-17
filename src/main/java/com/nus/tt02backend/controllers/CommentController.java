package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Comment;
import com.nus.tt02backend.models.Post;
import com.nus.tt02backend.services.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;

    @PostMapping("/createComment/{postId}/{parentCommentId}/{userId}")
    public ResponseEntity<Comment> createPost(@PathVariable Long postId, @PathVariable Long parentCommentId,
                                           @PathVariable Long userId, @RequestBody Comment commentToCreate)
            throws BadRequestException {
        Comment comment = commentService.createComment(postId, parentCommentId, userId, commentToCreate);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/updateComment")
    public ResponseEntity<Comment> updateComment(@RequestBody Comment commentToUpdate)
            throws BadRequestException {
        Comment comment = commentService.updateComment(commentToUpdate);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/deleteComment/{commentIdToDelete}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentIdToDelete)
            throws BadRequestException {
        String responseMessage = commentService.deleteComment(commentIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }
}
