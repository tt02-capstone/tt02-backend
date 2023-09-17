package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    PaymentRepository paymentRepository;

    public VendorStaff retrieveVendor(Long vendorStaffId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorStaffId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

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

    public Tourist findTourist(Long touristId) throws NotFoundException {
        try {
            Tourist tourist = touristRepository.getTouristByUserId(touristId);
            if (tourist != null) {
                return tourist;
            } else {
                throw new NotFoundException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Local findLocal(Long localId) throws NotFoundException {
        try {
            Local local = localRepository.getLocalByUserId(localId);
            if (local != null) {
                return local;
            } else {
                throw new NotFoundException("Local not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public List<Booking> getAllBookingsByUser(Long userId) throws NotFoundException, BadRequestException {
        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);
        List<Booking> bookings = new ArrayList<Booking>();
        List<Booking> bookingsToReturn = new ArrayList<Booking>();

        if (currentUser.getUser_type().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            bookings = tourist.getBooking_list();
        } else if (currentUser.getUser_type().equals(localType)) {
            Local local = findLocal(userId);
            bookings = local.getBooking_list();
        } else {
            throw new BadRequestException("Current user type not tourist or local");
        }

        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatusEnum.CANCELLED) {
                if (booking.getStart_datetime().toLocalDate().isEqual(LocalDate.now())) {
                    booking.setStatus(BookingStatusEnum.ONGOING);
                } else if (booking.getStart_datetime().toLocalDate().isBefore(LocalDate.now())) {
                    booking.setStatus(BookingStatusEnum.COMPLETED);
                } else {
                    booking.setStatus(BookingStatusEnum.UPCOMING);
                }
            }
            bookingRepository.save(booking);
            bookingsToReturn.add(booking);
        }

        for (Booking booking : bookingsToReturn) {
            booking.setLocal_user(null);
            booking.setTourist_user(null);
            booking.getPayment().setBooking(null);
        }

        return bookingsToReturn;
    public List<Booking> retrieveAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingByBookingId(Long bookingId) throws NotFoundException {
        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

            if (bookingOptional.isPresent()) {
                Booking booking = bookingOptional.get();

                if (booking.getLocal_user() != null) {
                    Local local = booking.getLocal_user();
                    local.setBooking_list(null);
                } else if (booking.getTourist_user() != null) {
                    Tourist tourist = booking.getTourist_user();
                    tourist.setBooking_list(null);
                }
                booking.getPayment().setBooking(null);
                return booking;
            } else {
                throw new NotFoundException("Booking not found");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public String cancelBooking(Long bookingId) throws NotFoundException, BadRequestException {
        Booking booking = bookingRepository.getBookingByBookingId(bookingId);

        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking has already been cancelled!");
        }

        // refund
        if (Duration.between(booking.getStart_datetime(), LocalDateTime.now()).toDays() >= 3) {
            // refund 100%
        } else {
            // refund 0%
        }
        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);

        return "Booking successfully cancelled";
    }

    public List<Payment> getAllPaymentsByUser(Long userId) throws NotFoundException, BadRequestException {
        List<Booking> bookings = getAllBookingsByUser(userId);
        List<Payment> payments = new ArrayList<Payment>();

        for (Booking booking : bookings) {
            Payment payment = booking.getPayment();
            payment.setBooking(booking);
            payment.getBooking().setPayment(null);
            payments.add(payment);
        }

        return payments;
    }
    
    public List<Booking> getAllAttractionBookingsByVendor(Long vendorStaffId) throws NotFoundException {

        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        List<Booking> bookingsToReturn = new ArrayList<Booking>();
        List<Attraction> attractionList = new ArrayList<Attraction>();

        List<Booking> bookingList = retrieveAllBookings();

        if (!vendor.getAttraction_list().isEmpty()) {

            attractionList = vendor.getAttraction_list();

            for (Attraction attraction : attractionList) {

                Long attractionId = attraction.getAttraction_id();

                // for each booking that has the same attraction_id as attractionId, then add to list
                for (Booking booking : bookingList) {
                    if (booking.getAttraction().getAttraction_id() == attractionId) {
                        if (booking.getStatus() != BookingStatusEnum.CANCELLED) {
                            if (booking.getStart_datetime().toLocalDate().isEqual(LocalDate.now())) {
                                booking.setStatus(BookingStatusEnum.ONGOING);
                            } else if (booking.getStart_datetime().toLocalDate().isBefore(LocalDate.now())) {
                                booking.setStatus(BookingStatusEnum.COMPLETED);
                            } else {
                                booking.setStatus(BookingStatusEnum.UPCOMING);
                            }
                        }
                        bookingRepository.save(booking);
                        bookingsToReturn.add(booking);
                    }
                }
            }
        }

        for (Booking booking : bookingsToReturn) {
            if (booking.getLocal_user() != null) {
                Local local = booking.getLocal_user();
                local.setBooking_list(null);
            } else if (booking.getTourist_user() != null) {
                Tourist tourist = booking.getTourist_user();
                tourist.setBooking_list(null);
            }
            booking.getPayment().setBooking(null);
        }

        return bookingsToReturn;
    }

    public Booking getAttractionBookingByVendor(Long vendorStaffId, Long bookingId) throws NotFoundException {

        List<Booking> bookingList = getAllAttractionBookingsByVendor(vendorStaffId);

        for (Booking b : bookingList) {
            if (b.getBooking_id().equals(bookingId)) {
                if (b.getLocal_user() != null) {
                    Local local = b.getLocal_user();
                    local.setBooking_list(null);
                } else if (b.getTourist_user() != null) {
                    Tourist tourist = b.getTourist_user();
                    tourist.setBooking_list(null);
                }
                b.getPayment().setBooking(null);
                return b;
            }
        }
        throw new NotFoundException("Booking not found!"); // if the booking is not part of vendor's listing
    }

    // To be deleted - for testing purposes
    public String tempCreateBooking() throws NotFoundException {
        Booking booking = new Booking();
        booking.setStart_datetime(LocalDateTime.now().plusDays(4l));
        booking.setEnd_datetime(LocalDateTime.now().plusDays(5l));
        booking.setLast_update(LocalDateTime.now());
        booking.setStatus(BookingStatusEnum.UPCOMING);
        booking.setType(BookingTypeEnum.ATTRACTION);

        Attraction attraction = attractionRepository.findById(1l).get();
        booking.setAttraction(attraction);

        bookingRepository.save(booking);

        Payment payment = new Payment();
        payment.setPayment_amount(new BigDecimal("123"));
        payment.setIs_paid(true);
        payment.setBooking(booking);
        payment.setComission_percentage(new BigDecimal("0.1"));
        paymentRepository.save(payment);

//        Tourist tourist = findTourist(4l);
//        booking.setTourist_user(tourist);
        booking.setPayment(payment);
//        tourist.getBooking_list().add(booking);
//        touristRepository.save(tourist);

        Local local = findLocal(5l);
        booking.setLocal_user(local);
        local.getBooking_list().add(booking);
        localRepository.save(local);
        bookingRepository.save(booking);
        return "Success";

        // Tourist tourist = findTourist(2l);
        // booking.setTourist_user(tourist);
        // booking.setPayment(payment);
        // tourist.getBooking_list().add(booking);
        // bookingRepository.save(booking);
        // return "Success";
    }
}

