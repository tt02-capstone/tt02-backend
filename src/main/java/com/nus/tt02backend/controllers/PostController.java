package com.nus.tt02backend.controllers;

import com.nus.tt02backend.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/post")
public class PostController {
    @Autowired
    PostService postService;

//    @PostMapping("/create/{vendorId}")
//    public ResponseEntity<Telecom> create(@RequestBody Telecom telecomToCreate, @PathVariable Long vendorId) throws NotFoundException {
//        System.out.println(telecomToCreate);
//        Telecom telecom = telecomService.create(telecomToCreate, vendorId);
//        return ResponseEntity.ok(telecom);
//    }
//
//    @GetMapping("/getAllTelecomList")
//    public ResponseEntity<List<Telecom>> getAllTelecomeList() {
//        List<Telecom> telecomList = telecomService.getAllTelecomList();
//        return ResponseEntity.ok(telecomList);
//    }
}
