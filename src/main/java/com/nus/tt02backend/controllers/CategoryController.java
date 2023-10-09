package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.Category;
import com.nus.tt02backend.models.CategoryItem;
import com.nus.tt02backend.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> list = categoryService.getAllCategories();
        return ResponseEntity.ok(list);
    }
}
