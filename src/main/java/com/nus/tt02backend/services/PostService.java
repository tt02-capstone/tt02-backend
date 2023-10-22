package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PostService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    InternalStaffRepository internalStaffRepository;
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CategoryItemRepository categoryItemRepository;

    public Post createPost(Long userId, Long categoryItemId, Post postToCreate) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(categoryItemId);
        if (categoryItemOptional.isEmpty()) {
            throw new BadRequestException("Category item does not exist!");
        }

        postToCreate.setPublish_time(LocalDateTime.now());
        postToCreate.setUpdated_time(LocalDateTime.now());
        postToCreate.setComment_list(new ArrayList<>());

        if (postToCreate.getPost_image_list() == null) {
            postToCreate.setPost_image_list(new ArrayList<>());
        }
        Post post = postRepository.save(postToCreate);

        User user = userOptional.get();
        UserTypeEnum userType;
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.getPost_list().add(post);
            touristRepository.save(tourist);

            post.setTourist_user(tourist);
            postRepository.save(post);

            post.getTourist_user().setPost_list(null);
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.getPost_list().add(post);
            localRepository.save(local);

            post.setLocal_user(local);
            postRepository.save(post);

            post.getLocal_user().setPost_list(null);
        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            vendorStaff.getPost_list().add(post);
            vendorStaffRepository.save(vendorStaff);

            post.setVendor_staff_user(vendorStaff);
            postRepository.save(post);

            post.getVendor_staff_user().setPost_list(null);
            post.getVendor_staff_user().getVendor().setVendor_staff_list(null);

        } else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            internalStaff.getPost_list().add(post);
            internalStaffRepository.save(internalStaff);

            post.setInternal_staff_user(internalStaff);
            postRepository.save(post);

            post.getInternal_staff_user().setPost_list(null);
        }

        CategoryItem categoryItem = categoryItemOptional.get();
        categoryItem.getPost_list().add(post);
        categoryItemRepository.save(categoryItem);

        return post;
    }

    public Post updatePost(Post postToUpdate) throws BadRequestException {
        Optional<Post> postOptional = postRepository.findById(postToUpdate.getPost_id());

        if (postOptional.isEmpty()) {
            throw new BadRequestException("Post does not exist!");
        }

        Post post = postOptional.get();
        post.setTitle(postToUpdate.getTitle());
        post.setContent(postToUpdate.getContent());
        post.setUpdated_time(LocalDateTime.now());
        if (postToUpdate.getPost_image_list() == null) {
            post.setPost_image_list(new ArrayList<>());
        } else {
            post.setPost_image_list(postToUpdate.getPost_image_list());
        }
        postRepository.save(post);

        if (post.getTourist_user() != null) {
            post.getTourist_user().setPost_list(null);
        } else if (post.getLocal_user() != null) {
            post.getLocal_user().setPost_list(null);
        } else if (post.getVendor_staff_user() != null) {
            post.getVendor_staff_user().setPost_list(null);
            post.getVendor_staff_user().setVendor(null);
        } else if (post.getInternal_staff_user() != null) {
            post.getInternal_staff_user().setPost_list(null);
        }

        return post;
    }

    public String deletePost(Long postIdToDelete) throws BadRequestException {
        Optional<Post> postOptional = postRepository.findById(postIdToDelete);

        if (postOptional.isEmpty()) {
            throw new BadRequestException("Post does not exist!");
        }

        Post post = postOptional.get();
        if (!post.getComment_list().isEmpty()) {
            throw new BadRequestException("Only posts without comments can be deleted!");
        }

        if (post.getTourist_user() != null) {
            Tourist tourist = post.getTourist_user();
            tourist.getPost_list().remove(post);
            touristRepository.save(tourist);
        } else if (post.getLocal_user() != null) {
            Local local = post.getLocal_user();
            local.getPost_list().remove(post);
            localRepository.save(local);
        } else if (post.getVendor_staff_user() != null) {
            VendorStaff vendorStaff = post.getVendor_staff_user();
            vendorStaff.getPost_list().remove(post);
            vendorStaffRepository.save(vendorStaff);
        } else if (post.getInternal_staff_user() != null) {
            InternalStaff internalStaff = post.getInternal_staff_user();
            internalStaff.getPost_list().remove(post);
            internalStaffRepository.save(internalStaff);
        }

        List<CategoryItem> categoryItems = categoryItemRepository.findAll();
        for (CategoryItem categoryItem : categoryItems) {
            if (categoryItem.getPost_list().contains(post)) {
                categoryItem.getPost_list().remove(post);
                categoryItemRepository.save(categoryItem);
                break;
            }
        }

        postRepository.delete(post);

        return "Post successfully deleted";
    }

    public List<Post> getAllPostByCategoryItemId(Long id) throws NotFoundException {

        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(id);

        if (categoryItemOptional.isPresent()) {
            CategoryItem categoryItem = categoryItemOptional.get();
            List<Post> list = categoryItem.getPost_list();

            for (Post p : list) {
                p.setComment_list(null);
                if (p.getLocal_user() != null) {
                    p.getLocal_user().setPost_list(null);
                    p.getLocal_user().setComment_list(null);
                    p.getLocal_user().setCart_list(null);
                    p.getLocal_user().setBooking_list(null);
                    p.getLocal_user().setSupport_ticket_list(null);
                }
                else if (p.getTourist_user() != null) {
                    p.getTourist_user().setPost_list(null);
                    p.getTourist_user().setComment_list(null);
                    p.getTourist_user().setCart_list(null);
                    p.getTourist_user().setBooking_list(null);
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
                    p.getVendor_staff_user().getVendor().setVendor_staff_list(null);
                    p.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    p.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                }
            }

            return list;
        } else {
            throw new NotFoundException("Category item not found!");
        }
    }

    public Post getPost(Long id) throws NotFoundException {

        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isPresent()) {
            Post p = postOptional.get();

            List<Comment> childComments = new ArrayList<>();
            if (!p.getComment_list().isEmpty()) {
                for (Comment comment : p.getComment_list()) {
                    recursiveCheck(comment);
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
                p.getVendor_staff_user().getVendor().setVendor_staff_list(null);
                p.getVendor_staff_user().setIncoming_support_ticket_list(null);
                p.getVendor_staff_user().setOutgoing_support_ticket_list(null);
            }

            p.getComment_list().removeAll(childComments);

            return p;
        } else {
            throw new NotFoundException("Post not found!");
        }
    }

    public Post upvote(Long userId, Long postId) throws NotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Post> postOptional = postRepository.findById(postId);

        if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
        if (postOptional.isEmpty()) throw new NotFoundException("Post not found!");

        User user = userOptional.get();
        Post post = postOptional.get();

        // list does not contain user
        if (!post.getUpvoted_user_id_list().contains(user.getUser_id())) {
            post.getUpvoted_user_id_list().add(user.getUser_id());
            post.getDownvoted_user_id_list().remove(user.getUser_id());

        } else { // contains user
            post.getUpvoted_user_id_list().remove(user.getUser_id());
        }

        postRepository.save(post);

        post.setComment_list(null);
        post.setInternal_staff_user(null);
        post.setTourist_user(null);
        post.setLocal_user(null);
        post.setVendor_staff_user(null);

        return post;
    }

    public Post downvote(Long userId, Long postId) throws NotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Post> postOptional = postRepository.findById(postId);

        if (userOptional.isEmpty()) throw new NotFoundException("User not found!");
        if (postOptional.isEmpty()) throw new NotFoundException("Post not found!");

        User user = userOptional.get();
        Post post = postOptional.get();

        // list does not contain user
        if (!post.getDownvoted_user_id_list().contains(user.getUser_id())) {
            post.getDownvoted_user_id_list().add(user.getUser_id());
            post.getUpvoted_user_id_list().remove(user.getUser_id());

        } else { // contains user
            post.getDownvoted_user_id_list().remove(user.getUser_id());
        }

        postRepository.save(post);

        post.setComment_list(null);
        post.setInternal_staff_user(null);
        post.setTourist_user(null);
        post.setLocal_user(null);
        post.setVendor_staff_user(null);

        return post;
    }

    private void recursiveCheck(Comment comment) {
        comment.setPost(null);
        comment.setParent_comment(null);

        if (comment.getLocal_user() != null) {
            comment.getLocal_user().setComment_list(null);
            comment.getLocal_user().setPost_list(null);
            comment.getLocal_user().setBooking_list(null);
            comment.getLocal_user().setCart_list(null);
            comment.getLocal_user().setTour_type_list(null);
        }
        if (comment.getTourist_user() != null) {
            comment.getTourist_user().setComment_list(null);
            comment.getTourist_user().setPost_list(null);
            comment.getTourist_user().setBooking_list(null);
            comment.getTourist_user().setCart_list(null);
            comment.getTourist_user().setTour_type_list(null);
        }

        if (comment.getVendor_staff_user() != null) {
            comment.getVendor_staff_user().setComment_list(null);
            comment.getVendor_staff_user().setPost_list(null);
            comment.getVendor_staff_user().setVendor(null);
        }

        if (comment.getInternal_staff_user() != null) {
            comment.getInternal_staff_user().setComment_list(null);
            comment.getInternal_staff_user().setPost_list(null);
        }

        for (Comment childComment : comment.getChild_comment_list()) {
            recursiveCheck(childComment);
        }
    }
}