package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/report")
public class ReportController {
    @Autowired
    ReportService reportService;

    @PostMapping("/reportPost/{postId}")
    public ResponseEntity<Report> reportPost(@PathVariable Long postId, @RequestBody Report newReport) throws NotFoundException {
        Report report = reportService.reportPost(postId, newReport);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/reportComment/{commentId}")
    public ResponseEntity<Report> reportComment(@PathVariable Long commentId, @RequestBody Report newReport) throws NotFoundException {
        Report report = reportService.reportComment(commentId, newReport);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/viewAllReportedPost")
    public ResponseEntity<List<Report>> viewAllReportedPost() throws NotFoundException {
        List<Report> rList = reportService.viewAllReportedPost();
        return ResponseEntity.ok(rList);
    }

    @GetMapping("/viewAllReportedComment")
    public ResponseEntity<List<Report>> viewAllReportedComment() throws NotFoundException {
        List<Report> rList = reportService.viewAllReportedComment();
        return ResponseEntity.ok(rList);
    }

    @PutMapping("/approveCommentReport/{reportId}/{commentId}")
    public ResponseEntity<Report> approveCommentReport(@PathVariable Long reportId, @PathVariable Long commentId ) throws NotFoundException {
        Report r = reportService.approveCommentReport(reportId, commentId);
        return  ResponseEntity.ok(r);
    }

    @PutMapping("/approvePostReport/{reportId}/{postId}")
    public ResponseEntity<Report> approvePostReport(@PathVariable Long reportId , @PathVariable Long postId ) throws NotFoundException {
        Report r = reportService.approvePostReport(reportId, postId);
        return  ResponseEntity.ok(r);
    }

    @PutMapping("/rejectReport/{reportId}")
    public ResponseEntity<Report> rejectReport(@PathVariable Long reportId ) throws NotFoundException {
        Report r = reportService.rejectReport(reportId);
        return  ResponseEntity.ok(r);
    }

    @PutMapping("/autoApproveCommentReport/{commentId}")
    public ResponseEntity<Comment> autoApproveCommentReport(@PathVariable Long commentId ) throws NotFoundException {
        Comment c = reportService.autoApproveCommentReport(commentId);
        return  ResponseEntity.ok(c);
    }

    @PutMapping("/autoApprovePostReport/{postId}")
    public ResponseEntity<Post> autoApprovePostReport(@PathVariable Long postId ) throws NotFoundException {
        Post p = reportService.autoApprovePostReport(postId);
        return  ResponseEntity.ok(p);
    }
}
