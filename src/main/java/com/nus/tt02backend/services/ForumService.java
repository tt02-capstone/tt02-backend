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
public class ForumService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryItemRepository categoryItemRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    InternalStaffRepository internalStaffRepository;

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
        postToCreate.setUpvote(0);
        postToCreate.setDownvote(0);
        postToCreate.setComment_list(new ArrayList<>());
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
}
