package com.nus.tt02backend.controllers;

import com.nus.tt02backend.services.CategoryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/categoryItem")
public class CategoryItemController {
    @Autowired
    CategoryItemService categoryItemService;

}
