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
}
