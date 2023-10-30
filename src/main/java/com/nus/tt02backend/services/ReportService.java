package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nus.tt02backend.models.*;

import java.time.LocalDateTime;
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
            newReport.setIs_resolved(false);
            reportRepository.save(newReport);

//            newReport.setReported_post(null);
//            newReport.setReported_comment(null);
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
            newReport.setIs_resolved(false);
            reportRepository.save(newReport);

//            newReport.setReported_post(null);
//            newReport.setReported_comment(null);
            return newReport;
        } else {
            throw new NotFoundException("Post not found!");
        }
    }

    public List<Report> viewAllReportedPost() throws NotFoundException  {
        List<Report> reportList = reportRepository.findAll();
        List<Report> postList = new ArrayList<>();
        for (Report r : reportList) {
            if (r.getReported_post() != null) {
                recursiveCheckPost(r.getReported_post()); // unbind rs
                postList.add(r);
            }
        }

        if (postList.isEmpty()) {
            throw new NotFoundException("No reported posts!");
        } else {
            return postList;
        }
    }

    public List<Report> viewAllReportedComment() throws NotFoundException  {
        List<Report> reportList = reportRepository.findAll();
        List<Report> commentList = new ArrayList<>();
        for (Report r : reportList) {
            if (r.getReported_comment() != null) {
                recursiveCheckComment(r.getReported_comment()); // unbind rs
                commentList.add(r);
            }
        }

        if (commentList.isEmpty()) {
            throw new NotFoundException("No reported comments!");
        } else {
            return commentList;
        }
    }

    public Report approveCommentReport(Long reportId, Long commentId) throws NotFoundException {
        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isPresent()) {
            Report r = reportOptional.get();
            System.out.println("at comment");
            r.setIs_resolved(true);

            Optional<Comment> commentOptional = commentRepository.findById(commentId);
            Comment comment = commentOptional.get();
            comment.setContent("[comment removed by admin]");
            comment.setIs_published(false); // to show that this is reported
            commentRepository.save(comment);

            r.setReported_comment(comment);
            reportRepository.save(r);

            recursiveCheckComment(r.getReported_comment());

            return r;
        } else {
            throw new NotFoundException("Report not found!");
        }
    }

    public Report approvePostReport(Long reportId, Long postId) throws NotFoundException {
        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isPresent()) {
            Report r = reportOptional.get();

            r.setIs_resolved(true);

            Optional<Post> postOptional = postRepository.findById(postId);
            Post post = postOptional.get();
            post.setIs_published(false); // unpublished the listing
            postRepository.save(post);

            r.setReported_post(post);
            reportRepository.save(r);

            recursiveCheckPost(r.getReported_post());

            return r;
        } else {
            throw new NotFoundException("Report not found!");
        }
    }

    public Report rejectReport(Long reportId) throws NotFoundException {
        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isPresent()) {
            Report r = reportOptional.get();
            r.setIs_resolved(true);
            reportRepository.save(r);

            if (r.getReported_comment() != null) {
                recursiveCheckComment(r.getReported_comment());
            } else {
                recursiveCheckPost(r.getReported_post());
            }

            return r;
        } else {
            throw new NotFoundException("Report not found!");
        }
    }

    private void recursiveCheckPost(Post p) {

        List<Comment> childComments = new ArrayList<>();
        if (!p.getComment_list().isEmpty()) {
            for (Comment comment : p.getComment_list()) {
                recursiveCheckComment(comment);
            }
        }

        if (p.getLocal_user() != null) {
            p.getLocal_user().setPost_list(null);
            p.getLocal_user().setComment_list(null);
            p.getLocal_user().setCart_list(null);
            p.getLocal_user().setBooking_list(null);
            p.getLocal_user().setTour_type_list(null);
            p.getLocal_user().setSupport_ticket_list(null);
        }
        else if (p.getTourist_user() != null) {
            p.getTourist_user().setPost_list(null);
            p.getTourist_user().setComment_list(null);
            p.getTourist_user().setCart_list(null);
            p.getTourist_user().setBooking_list(null);
            p.getTourist_user().setTour_type_list(null);
            p.getTourist_user().setSupport_ticket_list(null);
        }
        else if (p.getInternal_staff_user() != null) {
            p.getInternal_staff_user().setPost_list(null);
            p.getInternal_staff_user().setComment_list(null);
            p.getInternal_staff_user().setSupport_ticket_list(null);
        }
        else if (p.getVendor_staff_user() != null) {
            p.getVendor_staff_user().setPost_list(null);
            p.getVendor_staff_user().setComment_list(null);
            p.getVendor_staff_user().setVendor(null);
            p.getVendor_staff_user().setIncoming_support_ticket_list(null);
            p.getVendor_staff_user().setOutgoing_support_ticket_list(null);
            // p.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        }

         p.getComment_list().removeAll(childComments);
    }
    private void recursiveCheckComment(Comment comment) {

//        comment.getPost().setComment_list(null);
//        if (comment.getPost().getVendor_staff_user() != null) {
//            comment.getPost().getVendor_staff_user().setVendor(null);
//            comment.getPost().getVendor_staff_user().setOutgoing_support_ticket_list(null);
//            comment.getPost().getVendor_staff_user().setIncoming_support_ticket_list(null);
//            comment.getPost().getVendor_staff_user().setComment_list(null);
//        }
//        recursiveCheckPost(comment.getPost());

        comment.setPost(null);
        comment.setParent_comment(null);

        if (comment.getLocal_user() != null) {
            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setComment_list(null);
            comment.getLocal_user().setCart_list(null);
            comment.getLocal_user().setTour_type_list(null);
            comment.getLocal_user().setBooking_list(null);
            comment.getLocal_user().setSupport_ticket_list(null);
        }
        if (comment.getTourist_user() != null) {
            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setComment_list(null);
            comment.getTourist_user().setCart_list(null);
            comment.getTourist_user().setTour_type_list(null);
            comment.getTourist_user().setBooking_list(null);
            comment.getTourist_user().setSupport_ticket_list(null);
        }

        if (comment.getVendor_staff_user() != null) {
            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setVendor(null);
            comment.getVendor_staff_user().setOutgoing_support_ticket_list(null);
            comment.getVendor_staff_user().setIncoming_support_ticket_list(null);
            comment.getVendor_staff_user().setComment_list(null);
            // comment.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        }

        if (comment.getInternal_staff_user() != null) {
            comment.getInternal_staff_user().setPost_list(null);
            comment.getInternal_staff_user().setComment_list(null);
            comment.getInternal_staff_user().setSupport_ticket_list(null);
        }

        for (Comment childComment : comment.getChild_comment_list()) {
            recursiveCheckComment(childComment);
        }
    }

}