package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CategoryItemRepository categoryItemRepository;

    public List<Post> getAllPostByCategoryItemId(Long id) throws NotFoundException {

        Optional<CategoryItem> categoryItemOptional = categoryItemRepository.findById(id);

        if (categoryItemOptional.isPresent()) {
            CategoryItem categoryItem = categoryItemOptional.get();
            List<Post> list = categoryItem.getPost_list();

            for (Post p : list) {
                p.setComment_list(null);
                p.setLocal_user(null);
                p.setTourist_user(null);
                p.setVendor_staff_user(null);
                p.setInternal_staff_user(null);
            }

            return list;
        } else {
            throw new NotFoundException("Category item not found!");
        }
    }

    public Post getPost(Long id) throws NotFoundException {

        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            post.setComment_list(null); // might change in future
            post.setTourist_user(null); // might change in future
            post.setLocal_user(null); // might change in future
            post.setVendor_staff_user(null); // might change in future
            post.setInternal_staff_user(null); // might change in future

            return post;
        } else {
            throw new NotFoundException("Post not found!");
        }
    }
}