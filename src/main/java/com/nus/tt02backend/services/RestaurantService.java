package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RestaurantService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    DishRepository dishRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    public User findUser(Long userId) throws NotFoundException {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                throw new NotFoundException("User not found!");
            }
        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public VendorStaff retrieveVendor(Long vendorStaffId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorStaffId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public List<Restaurant> getAllRestaurant() { // for admin
        return restaurantRepository.findAll();
    }

    public List<Restaurant> getAllPublishedRestaurant() { // for mobile view
        List<Restaurant> rList = getAllRestaurant();
        List<Restaurant> publishedList = new ArrayList<>();
        for (Restaurant r : rList) {
            if (r.getIs_published() == Boolean.TRUE) {
                publishedList.add(r);
            }
        }
        return publishedList;
    }

    public Restaurant getRestaurant(Long restId) throws NotFoundException {
        try {
            Optional<Restaurant> rOptional = restaurantRepository.findById(restId);
            if (rOptional.isPresent()) {
                return rOptional.get();
            } else {
                throw new NotFoundException("Restaurant not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<Restaurant> getAllRestaurantByVendor(Long vendorStaffId) throws NotFoundException { // for vendor to view their own listing
        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        if (!vendor.getRestaurant_list().isEmpty()) {
            return vendor.getRestaurant_list();
        } else {
            throw new NotFoundException("Attractions not found!");
        }
    }

    public List<Dish> getRestaurantDish(Long restId) throws NotFoundException {
        Restaurant rest = getRestaurant((restId));
        if (!rest.getDish_list().isEmpty()) {
            return rest.getDish_list();
        } else {
            throw new NotFoundException("No dish tagged to this restaurant!");
        }
    }

    public Restaurant createRestaurant(VendorStaff vendorStaff, Restaurant restToCreate) throws BadRequestException {
        Restaurant checkR = restaurantRepository.getRestaurantByName(restToCreate.getName());

        if (checkR != null) {
            throw new BadRequestException("There is an restaurant listing with the same name, please choose another name!");
        }

        restToCreate.setEstimated_price_tier(PriceTierEnum.TIER_0); // set to be 0 cus price is tagged to dish not restaurant
        Restaurant newR = restaurantRepository.save(restToCreate);

        Vendor vendor = vendorStaff.getVendor();
        List<Restaurant> currentList = vendor.getRestaurant_list();
        currentList.add(newR);
        vendor.setRestaurant_list(currentList);

        System.out.println(vendor.getRestaurant_list());

        vendorStaffRepository.save(vendorStaff);

        return newR;
    }

    public Restaurant updateRestaurant(Restaurant restUpdate) throws NotFoundException {
        Restaurant r = restaurantRepository.findById(restUpdate.getRestaurant_id())
                .orElseThrow(() -> new NotFoundException("Restaurant Not Found!"));

        r.setName(restUpdate.getName());
        r.setDescription(restUpdate.getDescription());
        r.setAddress(restUpdate.getAddress());
        r.setOpening_hours(restUpdate.getOpening_hours());
        r.setContact_num(restUpdate.getContact_num());
        r.setIs_published(restUpdate.getIs_published());
        r.setSuggested_duration(restUpdate.getSuggested_duration());
        r.setRestaurant_type(restUpdate.getRestaurant_type());
        r.setRestaurant_image_list(restUpdate.getRestaurant_image_list()); // skip price tier n dish list

        restaurantRepository.save(r);

        return r;
    }

    public Dish getDish(Long dishId) throws NotFoundException {
        Dish d = dishRepository.findById(dishId)
                .orElseThrow(() -> new NotFoundException("Dish Not Found!"));
        return d;
    }

    public Dish addDish(Long restId, Dish newDish) throws BadRequestException, NotFoundException {
        Restaurant r = getRestaurant(restId);
        if (!r.getDish_list().isEmpty()) {
            for (Dish d : r.getDish_list()) {
                if (d.getName().equals(newDish.getName())) {
                    throw new BadRequestException("Dish name already exist! Please use another name!");
                }
            }
        }

        Dish dish = dishRepository.save(newDish);
        r.getDish_list().add(dish);
        updatePriceTier(dish);
        return dish;
    }

    // maybe change spice to be boolean instead of int
    public Dish updateDish(Long restId, Dish updateDish) throws NotFoundException {
        Restaurant r = getRestaurant(restId);
        Dish d =  getDish(updateDish.getDish_id());

    }

    // update dish

    // delete dish

    // pricing tier magic need to call for this everytime during dish CRUD n update to rest + set the new price tier to the rest
    public void updatePriceTier(Dish dish) {

    }

    public List<Restaurant> getAllSavedRestaurantForUser(Long userId) throws NotFoundException, BadRequestException {
        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);

        if (currentUser.getUser_type().equals(touristType)) {
            return ((Tourist) currentUser).getRestaurant_list();
        } else if (currentUser.getUser_type().equals(localType)) {
            return ((Local) currentUser).getRestaurant_list();
        } else {
            throw new BadRequestException("Current user type not tourist or local");
        }
    }

    public Restaurant saveRestaurantForUser(Long userId, Long restId) throws BadRequestException, NotFoundException {
        Restaurant restToSave = getRestaurant(restId);
        User currentUser = findUser(userId);
        List<Restaurant> savedR = new ArrayList<>();

        if (currentUser.getUser_type().equals(UserTypeEnum.TOURIST)) {
            savedR = ((Tourist) currentUser).getRestaurant_list();
        } else if (currentUser.getUser_type().equals(UserTypeEnum.LOCAL)) {
            savedR = ((Local) currentUser).getRestaurant_list();
        } else {
            throw new BadRequestException("Invalid User Type! Only Local or Tourist can save a restaurant!");
        }

        for (Restaurant r : savedR) {
            if (r.getRestaurant_id().equals(restId)) {
                throw new BadRequestException("You have already saved this restaurant!");
            }
        }

        savedR.add(restToSave);
        userRepository.save(currentUser);

        return restToSave;
    }

    public List<Restaurant> removeSavedRestaurantForUser(Long userId, Long restId) throws NotFoundException, BadRequestException {
        User currentUser = findUser(userId);
        List<Restaurant> currentSavedR = getAllSavedRestaurantForUser(userId);

        for (Restaurant r : currentSavedR) {
            if (r.getRestaurant_id().equals(restId)) {
                currentSavedR.remove(r);
                if (currentUser.getUser_type() == UserTypeEnum.TOURIST) {
                    ((Tourist) currentUser).setRestaurant_list(currentSavedR);
                    touristRepository.save((Tourist) currentUser);
                    return currentSavedR;

                } else if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
                    ((Local) currentUser).setRestaurant_list(currentSavedR);
                    localRepository.save((Local) currentUser);
                    return currentSavedR;
                }
            }
        }

        throw new NotFoundException("Restaurant not found in the saved list!");
    }

    public List<Restaurant> nearbyRestaurantReccom (GenericLocationEnum locationNow) throws NotFoundException {
        List<Restaurant> rList = getAllPublishedRestaurant();
        List<Restaurant> filterList = new ArrayList<>();

        if (rList.isEmpty()) {
            throw new NotFoundException("No restaurants are created!");
        } else {
            for (Restaurant r : rList) {
                if (r.getGeneric_location() == locationNow) {
                    filterList.add(r);
                }
            }
        }

        if (filterList.isEmpty()) {
            throw new NotFoundException("No restaurant to recommend near this location!");
        } else {
            return filterList; // will do the shuffling in another method
        }
    }



}
