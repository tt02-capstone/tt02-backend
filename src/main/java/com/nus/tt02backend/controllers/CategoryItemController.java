package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.services.CategoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/categoryItem")
public class CategoryItemController {
    @Autowired
    CategoryItemService categoryItemService;

    @PostMapping("/createCategoryItem/{categoryId}")
    public ResponseEntity<CategoryItem> createCategoryItem(@PathVariable Long categoryId, @RequestBody CategoryItem categoryItemToCreate)
            throws BadRequestException {
        CategoryItem categoryItem = categoryItemService.createCategoryItem(categoryId, categoryItemToCreate);
        return ResponseEntity.ok(categoryItem);
    }

    @PutMapping("/updateCategoryItem")
    public ResponseEntity<CategoryItem> updateCategoryItem(@RequestBody CategoryItem categoryItemToUpdate)
            throws BadRequestException {
        CategoryItem categoryItem = categoryItemService.updateCategoryItem(categoryItemToUpdate);
        return ResponseEntity.ok(categoryItem);
    }

    @DeleteMapping("/deleteCategoryItem/{categoryItemIdToDelete}")
    public ResponseEntity<String> deleteCategoryItem(@PathVariable Long categoryItemIdToDelete) throws BadRequestException {
        String responseMessage = categoryItemService.deleteCategoryItem(categoryItemIdToDelete);
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/getAllByCategoryId/{categoryId}")
    public ResponseEntity<List<CategoryItem>> getByCategoryId(@PathVariable Long categoryId) throws NotFoundException {
        List<CategoryItem> list = categoryItemService.getAllByCategoryId(categoryId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getLastCategoryItemId")
    public ResponseEntity<?> getLastCategoryItemId() {
        try {
            Long lastCategoryItemId = categoryItemService.getLastCategoryItemId();
            return ResponseEntity.ok(lastCategoryItemId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
