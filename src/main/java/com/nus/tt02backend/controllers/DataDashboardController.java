package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.CartBooking;
import com.nus.tt02backend.services.DataDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/data")
public class DataDashboardController {

    @Autowired
    DataDashboardService dataDashboardService;


    @GetMapping("/getData/{vendor_id}")
    public ResponseEntity<List<Object[]>> getData(@PathVariable String vendor_id) throws NotFoundException {

        List<Object[]> data = dataDashboardService.getData(vendor_id);
        return ResponseEntity.ok(data);
    }


}
