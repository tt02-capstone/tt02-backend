package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.repositories.*;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nus.tt02backend.models.*;

import java.util.*;

@Service
public class ReportService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    ReportRepository reportRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    public Report reportPost(Long postId, Report newReport) throws NotFoundException {

        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            newReport.setReported_post(post);
            reportRepository.save(newReport);

            newReport.setReported_post(null);
            newReport.setReported_comment(null);
            return newReport;
        } else {
            throw new NotFoundException("Post not found!");
        }
    }

    public Report reportComment(Long commentId, Report newReport) throws NotFoundException {

        Optional<Comment> commentOptional = commentRepository.findById(commentId);

        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            newReport.setReported_comment(comment);
            reportRepository.save(newReport);

            newReport.setReported_post(null);
            newReport.setReported_comment(null);
            return newReport;
        } else {
            throw new NotFoundException("Post not found!");
        }
    }
}