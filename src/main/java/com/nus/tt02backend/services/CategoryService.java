package com.nus.tt02backend.services;

import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    CategoryRepository categoryRepository;

    public Category create(Category category) {
        categoryRepository.save(category);
        return category;
    }

    public List<Category> getAllCategories() {
        List<Category> list = categoryRepository.getAllPublishedCategory();

        for (Category c : list) {
            for (CategoryItem i : c.getCategory_item_list()) {
                i.setPost_list(null);
            }
        }

        return list;
    }
}