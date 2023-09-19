package com.nus.tt02backend.controllers;


import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Booking;
import com.nus.tt02backend.models.CartBooking;
import com.nus.tt02backend.models.CartItem;
import com.nus.tt02backend.services.CartService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/cart")
public class CartController {

    @Autowired
    CartService cartService;

    @PostMapping("/addCartItems/{user_type}/{tourist_email}/{activity_name}")
    public ResponseEntity<Long> addCartItems(@PathVariable String tourist_email,
                                             @PathVariable String activity_name,
                                             @RequestBody List<CartItem> cartItems) throws NotFoundException, BadRequestException {
        Long bookingId = cartService.addCartItems(tourist_email, activity_name, cartItems);


        return ResponseEntity.ok(bookingId);
    }


    @GetMapping("/viewCart/{user_type}/{tourist_email}")
    public ResponseEntity<List<CartBooking>> viewCart(@PathVariable String user_type,
                                                      @PathVariable String tourist_email) {

        List<CartBooking> cartBookings = cartService.viewCart(user_type, tourist_email);

        return ResponseEntity.ok(cartBookings);
    }

    @PutMapping("/deleteCartItems/{user_type}/{tourist_email}")
    public ResponseEntity<List<Long>> deleteCartItems(@PathVariable String user_type,
                                                      @PathVariable String tourist_email,
                                                      @RequestBody List<Long> cart_item_ids) {

        List<Long> deleted_ids = cartService.deleteCartItems(user_type,tourist_email,cart_item_ids);


        return ResponseEntity.ok(deleted_ids);
    }

    @PutMapping("/updateCartItem/{cart_item_id}/{quantity}")
    public ResponseEntity<Long> updateCartItem(
            @PathVariable Long cart_item_id,
            @PathVariable Integer quantity) {


        Long updated_cart_item_id = cartService.updateCartItem(cart_item_id, quantity);

        return null;
    }

    @PostMapping("/checkout/{user_type}/{tourist_email}/{payment_method_id}/{totalPrice}")
    public ResponseEntity<List<Long>> checkout(@PathVariable String user_type,
                                               @PathVariable String tourist_email,
                                               @PathVariable String payment_method_id,
                                               @PathVariable Float totalPrice,
                                               @RequestBody List<Long> booking_ids) throws StripeException {


        List<Long> createdBookingIds =  cartService.checkout(user_type,tourist_email,
                payment_method_id, totalPrice,booking_ids);



        return ResponseEntity.ok(createdBookingIds);
    }

}