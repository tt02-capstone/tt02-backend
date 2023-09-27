package com.nus.tt02backend.controllers;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.models.Dish;
import com.nus.tt02backend.models.Restaurant;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.services.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    RestaurantService restaurantService;

    @GetMapping("/getAllRestaurant")
    public ResponseEntity<List<Restaurant>> getAllRestaurant() {
        List<Restaurant> rList = restaurantService.getAllRestaurant();
        return ResponseEntity.ok(rList);
    }

    @GetMapping("/getAllPublishedRestaurant")
    public ResponseEntity<List<Restaurant>> getAllPublishedRestaurant() {
        List<Restaurant> rList = restaurantService.getAllPublishedRestaurant();
        return ResponseEntity.ok(rList);
    }

    @GetMapping("/getRestaurant/{restId}")
    public ResponseEntity<Restaurant> getRestaurant(@PathVariable Long restId) throws NotFoundException {
        Restaurant r = restaurantService.getRestaurant(restId);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/getAllRestaurantByVendor/{vendorId}")
    public ResponseEntity<List<Restaurant>> getAllRestaurantByVendor(@PathVariable Long vendorId) throws NotFoundException {
        List<Restaurant> rList = restaurantService.getAllRestaurantByVendor(vendorId);
        return ResponseEntity.ok(rList);
    }

    @GetMapping("/getRestaurantDish/{restId}")
    public ResponseEntity<List<Dish>> getRestaurantDish(@PathVariable Long restId) throws NotFoundException {
        List<Dish> dList = restaurantService.getRestaurantDish(restId);
        return ResponseEntity.ok(dList);
    }

    @PostMapping ("/createRestaurant/{vendorId}")
    public ResponseEntity<Restaurant> createRestaurant(@PathVariable Long vendorId ,@RequestBody Restaurant restToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        VendorStaff vendorStaff = restaurantService.retrieveVendor(vendorId);
        Restaurant r =  restaurantService.createRestaurant(vendorStaff,restToCreate);
        return ResponseEntity.ok(r);
    }

    @PutMapping ("/updateRestaurant")
    public ResponseEntity<Restaurant> updateRestaurant(@RequestBody Restaurant restToUpdate)
            throws NotFoundException {
        Restaurant r =  restaurantService.updateRestaurant(restToUpdate);
        return ResponseEntity.ok(r);
    }

    @PostMapping ("/addDish/{restId}")
    public ResponseEntity<Dish> addDish(@PathVariable Long restId ,@RequestBody Dish newDish)
            throws BadRequestException, NotFoundException {
        Dish d =  restaurantService.addDish(restId,newDish);
        return ResponseEntity.ok(d);
    }

    @PutMapping ("/updateDish/{restId}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long restId ,@RequestBody Dish updateDish)
            throws NotFoundException {
        Dish d =  restaurantService.updateDish(restId,updateDish);
        return ResponseEntity.ok(d);
    }

    @DeleteMapping ("/deleteDish/{restId}/{dishId}")
    public ResponseEntity<List<Dish>> deleteDish(@PathVariable Long restId ,@PathVariable Long dishId)
            throws NotFoundException {
        List<Dish> dList =  restaurantService.deleteDish(restId,dishId);
        return ResponseEntity.ok(dList);
    }

    @GetMapping("/getAllSavedRestaurantForUser/{userId}")
    public ResponseEntity<List<Restaurant>> getAllSavedRestaurantForUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Restaurant> rList = restaurantService.getAllSavedRestaurantForUser(userId);
        return ResponseEntity.ok(rList);
    }

    @PutMapping("/saveRestaurantForUser/{userId}/{restId}")
    public ResponseEntity<List<Restaurant>> saveRestaurantForUser(@PathVariable Long userId, @PathVariable Long restId) throws NotFoundException, BadRequestException {
        List<Restaurant> rList = restaurantService.saveRestaurantForUser(userId,restId);
        return ResponseEntity.ok(rList);
    }

    @DeleteMapping("/removeSavedRestaurantForUser/{userId}/{restId}")
    public ResponseEntity<List<Restaurant>> removeSavedRestaurantForUser(@PathVariable Long userId, @PathVariable Long restId) throws NotFoundException, BadRequestException {
        List<Restaurant> rList = restaurantService.removeSavedRestaurantForUser(userId,restId);
        return ResponseEntity.ok(rList);
    }

    @GetMapping("/nearbyRestaurantReccom/{locationEnum}")
    public ResponseEntity<List<Restaurant>> nearbyRestaurantReccom(@PathVariable GenericLocationEnum locationEnum) throws NotFoundException {
        System.out.println("testing");
        System.out.println(locationEnum);
        List<Restaurant> rList = restaurantService.nearbyRestaurantReccom(locationEnum);
        return ResponseEntity.ok(rList);
    }

}
