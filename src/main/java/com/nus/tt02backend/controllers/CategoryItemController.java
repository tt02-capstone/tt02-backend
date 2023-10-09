package com.nus.tt02backend.controllers;

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

    @GetMapping("/getAllByCategoryId/{categoryId}")
    public ResponseEntity<List<CategoryItem>> getByCategoryId(@PathVariable Long categoryId) throws NotFoundException {
        List<CategoryItem> list = categoryItemService.getAllByCategoryId(categoryId);
        return ResponseEntity.ok(list);
    }
}
