package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    CategoryRepository categoryRepository;

    public CategoryItem createCategoryItem(Long categoryId, CategoryItem categoryItemToCreate) throws BadRequestException {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);

        if (categoryOptional.isEmpty()) {
            throw new BadRequestException("Category does not exist!");
        }

        categoryItemToCreate.setPost_list(new ArrayList<>());
        CategoryItem categoryItem = categoryItemRepository.save(categoryItemToCreate);

        Category category = categoryOptional.get();
        category.getCategory_item_list().add(categoryItem);
        categoryRepository.save(category);

        return categoryItem;
    }

    public CategoryItem updateCategoryItem(CategoryItem categoryItemToUpdate) throws BadRequestException {
        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(categoryItemToUpdate.getCategory_item_id());

        if (categoryItemOptional.isEmpty()) {
            throw new BadRequestException("Category item does not exist!");
        }

        CategoryItem categoryItem = categoryItemOptional.get();
        categoryItem.setName(categoryItemToUpdate.getName());
        categoryItem.setImage(categoryItemToUpdate.getImage());
        categoryItemRepository.save(categoryItem);

        return categoryItem;
    }

    public String deleteCategoryItem(Long categoryItemIdToDelete) throws BadRequestException {
        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(categoryItemIdToDelete);

        if (categoryItemOptional.isEmpty()) {
            throw new BadRequestException("Category item does not exist!");
        }

        CategoryItem categoryItem = categoryItemOptional.get();
        if (!categoryItem.getPost_list().isEmpty()) {
            throw new BadRequestException("Only category items without posts can be deleted!");
        }

        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            if (category.getCategory_item_list().contains(categoryItem)) {
                category.getCategory_item_list().remove(categoryItem);
                categoryRepository.save(category);
                break;
            }
        }

        categoryItemRepository.delete(categoryItem);

        return "Category item successfully deleted";
    }

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
        post.setContent(postToUpdate.getContent());
        post.setUpdated_time(LocalDateTime.now());
        post.getPost_image_list().clear();
        post.getPost_image_list().addAll(postToUpdate.getPost_image_list());
        postRepository.save(post);

        if (post.getTourist_user() != null) {
            post.getTourist_user().setPost_list(null);
        } else if (post.getLocal_user() != null) {
            post.getLocal_user().setPost_list(null);
        } else if (post.getVendor_staff_user() != null) {
            post.getVendor_staff_user().setPost_list(null);
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
}
