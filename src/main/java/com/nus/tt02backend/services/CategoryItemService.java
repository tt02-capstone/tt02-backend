package com.nus.tt02backend.services;

import com.amazonaws.services.kms.model.NotFoundException;
import com.nus.tt02backend.exceptions.BadRequestException;
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
}