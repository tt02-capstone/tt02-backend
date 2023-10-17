package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    InternalStaffRepository internalStaffRepository;

    public Comment createComment(Long postId, Long parentCommentId, Long userId, Comment commentToCreate)
            throws BadRequestException {
        // Retrieve post
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new BadRequestException("Post does not exist!");
        }
        Post post = postOptional.get();

        // Retrieve parent comment
        Comment parentComment = null;
        if (parentCommentId != 0L) {
            Optional<Comment> parentCommentOptional = commentRepository.findById(parentCommentId);
            if (parentCommentOptional.isEmpty()) {
                throw new BadRequestException("Parent comment does not exist!");
            }
            parentComment = parentCommentOptional.get();
        }

        // Retrieve user
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        // Set the default and necessary values
        if (parentComment != null) {
            commentToCreate.setParent_comment(parentComment);
        } else {
            commentToCreate.setParent_comment(null);
        }
        commentToCreate.setPost(post);
        commentToCreate.setPublish_time(LocalDateTime.now());
        commentToCreate.setUpdated_time(LocalDateTime.now());
        commentToCreate.setChild_comment_list(new ArrayList<>());
        commentToCreate.setUpvoted_user_id_list(new ArrayList<>());
        commentToCreate.setDownvoted_user_id_list(new ArrayList<>());
        commentToCreate.setReported_comment_list(new ArrayList<>());
        commentToCreate.setReply_list(new ArrayList<>());
        Comment comment = commentRepository.save(commentToCreate);

        // Add comment to post's comment list
        post.getComment_list().add(comment);
        postRepository.save(post);

        // Add child comment to parent comment
        if (parentComment != null) {
            parentComment.getChild_comment_list().add(comment);
            commentRepository.save(parentComment);
        }

        // Add comment to user's comment list
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.getComment_list().add(comment);
            touristRepository.save(tourist);

            comment.setTourist_user(tourist);
            commentRepository.save(comment);

            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setComment_list(null);
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.getComment_list().add(comment);
            localRepository.save(local);

            comment.setLocal_user(local);
            commentRepository.save(comment);

            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setComment_list(null);
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            vendorStaff.getComment_list().add(comment);
            vendorStaffRepository.save(vendorStaff);

            comment.setVendor_staff_user(vendorStaff);
            commentRepository.save(comment);

            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setComment_list(null);
            comment.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        } else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            internalStaff.getComment_list().add(comment);
            internalStaffRepository.save(internalStaff);

            comment.setInternal_staff_user(internalStaff);
            commentRepository.save(comment);

            comment.getInternal_staff_user().setPost_list(null);
            comment.getInternal_staff_user().setComment_list(null);
        }

        comment.getPost().setComment_list(null);
        comment.getPost().setTourist_user(null);
        comment.getPost().setLocal_user(null);
        comment.getPost().setVendor_staff_user(null);
        comment.getPost().setInternal_staff_user(null);
        comment.setParent_comment(null);

        return comment;
    }

    public Comment updateComment(Comment commentToUpdate) throws BadRequestException {
        Optional<Comment> commentOptional = commentRepository.findById(commentToUpdate.getComment_id());

        if (commentOptional.isEmpty()) {
            throw new BadRequestException("Comment does not exist!");
        }

        Comment comment = commentOptional.get();
        comment.setContent(commentToUpdate.getContent());
        comment.setUpdated_time(LocalDateTime.now());
        commentRepository.save(comment);

        if (comment.getTourist_user() != null) {
            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setComment_list(null);
        } else if (comment.getLocal_user() != null) {
            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setComment_list(null);
        } else if (comment.getVendor_staff_user() != null) {
            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setComment_list(null);
            comment.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        } else if (comment.getInternal_staff_user() != null) {
            comment.getInternal_staff_user().setPost_list(null);
            comment.getInternal_staff_user().setComment_list(null);
        }

        comment.getPost().setComment_list(null);
        comment.getPost().setTourist_user(null);
        comment.getPost().setLocal_user(null);
        comment.getPost().setVendor_staff_user(null);
        comment.getPost().setInternal_staff_user(null);
        comment.setParent_comment(null);

        return comment;
    }

    public String deleteComment(Long commentIdToDelete) throws BadRequestException {
        Optional<Comment> commentOptional = commentRepository.findById(commentIdToDelete);

        if (commentOptional.isEmpty()) {
            throw new BadRequestException("Comment does not exist!");
        }

        Comment comment = commentOptional.get();
        if (!comment.getChild_comment_list().isEmpty()) {
            throw new BadRequestException("Only comments without replies can be deleted!");
        }

        Post post = comment.getPost();
        post.getComment_list().remove(comment);
        postRepository.save(post);

        if (comment.getTourist_user() != null) {
            Tourist tourist = comment.getTourist_user();
            tourist.getComment_list().remove(comment);
            touristRepository.save(tourist);
        } else if (comment.getLocal_user() != null) {
            Local local = comment.getLocal_user();
            local.getComment_list().remove(comment);
            localRepository.save(local);
        } else if (comment.getVendor_staff_user() != null) {
            VendorStaff vendorStaff = comment.getVendor_staff_user();
            vendorStaff.getComment_list().remove(comment);
            vendorStaffRepository.save(vendorStaff);
        } else if (comment.getInternal_staff_user() != null) {
            InternalStaff internalStaff = comment.getInternal_staff_user();
            internalStaff.getComment_list().remove(comment);
            internalStaffRepository.save(internalStaff);
        }

        if (comment.getParent_comment() != null) {
            Comment parentComment = comment.getParent_comment();
            parentComment.getChild_comment_list().remove(comment);
            commentRepository.save(parentComment);
        }

        commentRepository.delete(comment);

        return "Comment successfully deleted";
    }
    
    public Comment upvoteComment(Long userId, Long commentId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new BadRequestException("Comment does not exist!");
        }

        User user = userOptional.get();
        Comment comment = commentOptional.get();
        if (!comment.getUpvoted_user_id_list().contains(user.getUser_id())) {
            comment.getUpvoted_user_id_list().add(user.getUser_id());
            comment.getDownvoted_user_id_list().remove(user.getUser_id());
        } else {
            comment.getUpvoted_user_id_list().remove(user.getUser_id());
        }
        commentRepository.save(comment);

        if (comment.getTourist_user() != null) {
            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setComment_list(null);
        } else if (comment.getLocal_user() != null) {
            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setComment_list(null);
        } else if (comment.getVendor_staff_user() != null) {
            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setComment_list(null);
            comment.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        } else if (comment.getInternal_staff_user() != null) {
            comment.getInternal_staff_user().setPost_list(null);
            comment.getInternal_staff_user().setComment_list(null);
        }

        comment.getPost().setComment_list(null);
        comment.getPost().setTourist_user(null);
        comment.getPost().setLocal_user(null);
        comment.getPost().setVendor_staff_user(null);
        comment.getPost().setInternal_staff_user(null);
        comment.setParent_comment(null);

        return comment;
    }

    public Comment downvoteComment(Long userId, Long commentId) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new BadRequestException("Comment does not exist!");
        }

        User user = userOptional.get();
        Comment comment = commentOptional.get();
        if (!comment.getDownvoted_user_id_list().contains(user.getUser_id())) {
            comment.getDownvoted_user_id_list().add(user.getUser_id());
            comment.getUpvoted_user_id_list().remove(user.getUser_id());
        } else {
            comment.getDownvoted_user_id_list().remove(user.getUser_id());
        }
        commentRepository.save(comment);

        if (comment.getTourist_user() != null) {
            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setComment_list(null);
        } else if (comment.getLocal_user() != null) {
            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setComment_list(null);
        } else if (comment.getVendor_staff_user() != null) {
            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setComment_list(null);
            comment.getVendor_staff_user().getVendor().setVendor_staff_list(null);
        } else if (comment.getInternal_staff_user() != null) {
            comment.getInternal_staff_user().setPost_list(null);
            comment.getInternal_staff_user().setComment_list(null);
        }

        comment.getPost().setComment_list(null);
        comment.getPost().setTourist_user(null);
        comment.getPost().setLocal_user(null);
        comment.getPost().setVendor_staff_user(null);
        comment.getPost().setInternal_staff_user(null);
        comment.setParent_comment(null);

        return comment;
    }
}