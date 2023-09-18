package com.nus.tt02backend.services;

import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import java.util.UUID;

@Service
public class CartService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingItemRepository bookingItemRepository;

    @Autowired
    CartBookingRepository cartBookingRepository;

    @Autowired
    PaymentRepository paymentRepository;

    public Long addCartItems(@PathVariable String tourist_email, @PathVariable String activity_name,
                             @RequestBody List<CartItem> cartItems) {


        // Get Tourist object based on email via Tourist Repository
        // Need official function to handle different cases
        Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

        // Need official function to handle different cases
        Attraction selectedAttraction = attractionRepository.getAttractionByName(activity_name);
        LocalDateTime start_dateTime = LocalDateTime.now();
        LocalDateTime end_dateTime = LocalDateTime.now();

        List<CartItem> addedCartItems = new ArrayList<>();
        for (CartItem cartItemToCreate : cartItems) {

            // Must check if CartItem with attraction name and type exists!


            CartItem newCartItem = cartItemRepository.save(cartItemToCreate);
            System.out.println(newCartItem.getCart_item_id());


            addedCartItems.add(newCartItem);

            System.out.println(addedCartItems.size());


        }

        // Check if booking exists
        CartBooking cartBookingToCreate = new CartBooking();
        cartBookingToCreate.setStart_datetime(start_dateTime);
        cartBookingToCreate.setEnd_datetime(end_dateTime);

        cartBookingToCreate.setType(BookingTypeEnum.ATTRACTION);
        cartBookingToCreate.setActivity_name(selectedAttraction.getName());
        cartBookingToCreate.setAttraction(selectedAttraction);
        cartBookingToCreate.setCart_item_list(addedCartItems);


        Long cartBookingId = cartBookingRepository.save(cartBookingToCreate).getCart_booking_id();

        List<CartBooking> currentCartBookings = currentTourist.getCart_list();
        currentCartBookings.add(cartBookingToCreate);
        currentTourist.setCart_list(currentCartBookings);

        localRepository.save(currentTourist);

        // Calculate total Cart amount?

        return cartBookingId;
    }


    public List<CartBooking> viewCart(String user_type, String tourist_email) {

        Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);


        return currentTourist.getCart_list();
    }

    public List<Long> deleteCartItems(String user_type, String tourist_email,
                                      List<Long> cart_item_ids) {

        Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);




        return cart_item_ids;
    }


    public Long updateCartItem(Long cart_item_id, Integer quantity) {

       return null;
    }

    public List<Long> checkout(String user_type, String tourist_email, String payment_method_id,
                               List<Long> booking_ids) throws StripeException {

        Local currentTourist = localRepository.retrieveLocalByEmail(tourist_email);

        // To include query of cartBookings by booking_ids and user

        List<CartBooking> bookingsToCheckout = currentTourist.getCart_list();

        BigDecimal totalAmountPayable = BigDecimal.valueOf(0); // Will retrieve this from FE

        List<Payment> processedPayments = new ArrayList<>();

        List<Long> createdBookings = new ArrayList<>();

        Map<String, Object> automaticPaymentMethods =
                new HashMap<>();
        automaticPaymentMethods.put("enabled", true);

        Map<String, Object> paymentParams = new HashMap<>();
        paymentParams.put("amount", totalAmountPayable);
        paymentParams.put("currency", "sgd");
        paymentParams.put(
                "automatic_payment_methods",
                automaticPaymentMethods
        );
        paymentParams.put(
                "confirm",
                false
        );
        paymentParams.put(
                "customer",
                "customer_id"
        );
        paymentParams.put(
                "payment_method",
                payment_method_id
        );

        for (CartBooking bookingToCheckout : bookingsToCheckout) {
            BigDecimal amountPayable = BigDecimal.valueOf(0);
            List<BookingItem> bookingItems = new ArrayList<>();
            Attraction selected_attraction = bookingToCheckout.getAttraction();
            for (CartItem cartItem: bookingToCheckout.getCart_item_list()) {
                amountPayable = amountPayable.add(cartItem.getPrice());
                BookingItem newBookingItem = new BookingItem();
                newBookingItem.setQuantity(cartItem.getQuantity());
                newBookingItem.setStart_datetime(cartItem.getStart_datetime());
                newBookingItem.setEnd_datetime(cartItem.getEnd_datetime());
                newBookingItem.setType(cartItem.getType());
                newBookingItem.setActivity_selection(cartItem.getActivity_selection());
                bookingItemRepository.save(newBookingItem);
                bookingItems.add(newBookingItem);

            }

            Booking newBooking = new Booking();
            newBooking.setStart_datetime(bookingToCheckout.getStart_datetime());
            newBooking.setEnd_datetime(bookingToCheckout.getEnd_datetime());
            newBooking.setLast_update(LocalDateTime.now());
            newBooking.setStatus(BookingStatusEnum.UPCOMING);
            newBooking.setType(bookingToCheckout.getType());
            newBooking.setActivity_name(bookingToCheckout.getActivity_name());
            newBooking.setAttraction(selected_attraction);
            newBooking.setLocal_user(currentTourist);
            bookingRepository.save(newBooking);


            Payment bookingPayment = new Payment();
            bookingPayment.setPayment_amount(totalAmountPayable);
            bookingPayment.setComission_percentage(BigDecimal.valueOf(0.10));
            bookingPayment.setIs_paid(true);

            paymentRepository.save(bookingPayment);

            bookingPayment.setBooking(newBooking);
            newBooking.setPayment(bookingPayment);
            bookingRepository.save(newBooking);
            bookingRepository.save(newBooking);

            createdBookings.add(newBooking.getBooking_id());
            processedPayments.add(bookingPayment);
        }
        System.out.println(createdBookings);

        //System.out.println(processedPayments);


        currentTourist.setCart_list(new ArrayList<>());

        localRepository.save(currentTourist);

        System.out.println("ASD");


        return createdBookings;
    }
}