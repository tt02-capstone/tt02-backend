package com.nus.tt02backend.services;

import com.amazonaws.services.kms.model.NotFoundException;
import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryItemService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryItemRepository categoryItemRepository;

    public List<CategoryItem> getAllByCategoryId(Long categoryId) throws NotFoundException {

        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);

        if (categoryOptional.isPresent()) {
            Category category = categoryOptional.get();
            List<CategoryItem> list = category.getCategory_item_list();

            for (CategoryItem i : list) {
                i.setPost_list(null);
            }

            return list;
        } else {
            throw new NotFoundException("Category not found!");
        }
    }

}