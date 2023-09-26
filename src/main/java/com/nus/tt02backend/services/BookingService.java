package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    BookingItemRepository bookingItemRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    TourRepository tourRepository;

    @Autowired
    TelecomRepository telecomRepository;

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
            // bookingRepository.save(booking);
            bookingsToReturn.add(booking);
        }

        for (Booking booking : bookingsToReturn) {
            booking.setLocal_user(null);
            booking.setTourist_user(null);
            booking.getPayment().setBooking(null);
        }

        return bookingsToReturn;
    }

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

    public String cancelBooking(Long bookingId) throws NotFoundException, BadRequestException, StripeException {
        Booking booking = bookingRepository.getBookingByBookingId(bookingId);

        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }

        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new BadRequestException("Booking has already been cancelled!");
        }

        if (Duration.between(LocalDateTime.now(),booking.getStart_datetime()).toDays() >= 3) {
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put(
                    "payment_intent",
                    booking.getPayment().getPayment_id()
            );

            Refund refund = Refund.create(refundParams);

            Payment payment = booking.getPayment();

            Attraction selected_attraction = booking.getAttraction();

            Vendor vendor = vendorRepository.findVendorByAttractionName(selected_attraction.getName());

            BigDecimal commission = payment.getPayment_amount().multiply(payment.getComission_percentage());

            BigDecimal payoutAmount = payment.getPayment_amount().subtract(commission);

            BigDecimal currentWalletBalance = vendor.getWallet_balance();

            vendor.setWallet_balance(currentWalletBalance.subtract(payoutAmount));

            vendorRepository.save(vendor);

        }

        booking.setStatus(BookingStatusEnum.CANCELLED);
        booking.setLast_update(LocalDateTime.now());
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
    
    public List<Booking> getAllBookingsByVendor(Long vendorStaffId) throws NotFoundException {

        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        List<Booking> bookingsToReturn = new ArrayList<Booking>();
        List<Booking> bookingList = retrieveAllBookings();

        for (Booking b : bookingList) {
            System.out.println("hey: " + b.getBooking_id());
            System.out.println(vendor.getTelecom_list().size());
            if (!vendor.getAttraction_list().isEmpty()) {
                // attraction
                for (Attraction a : vendor.getAttraction_list()) {
                    if (b.getAttraction() != null && b.getAttraction().getAttraction_id() == a.getAttraction_id()) {
                        System.out.println("aaaa");
                        Booking temp = this.setBookingStatus(b);
                        bookingRepository.save(temp);
                        bookingsToReturn.add(temp);
                    }
                }
            }

            // telecom
            if (!vendor.getTelecom_list().isEmpty()) {
                for (Telecom t : vendor.getTelecom_list()) {
                    if (b.getTelecom() != null && b.getTelecom().getTelecom_id() == t.getTelecom_id()) {
                        System.out.println("bbbb");
                        Booking temp = this.setBookingStatus(b);
                        bookingRepository.save(temp);
                        bookingsToReturn.add(temp);
                    }
                }
            }
        }

        // setting of 2 way relationship to null
        for (Booking b : bookingsToReturn) {
            if (b.getLocal_user() != null) {
                Local local = b.getLocal_user();
                local.setBooking_list(null);
            } else if (b.getTourist_user() != null) {
                Tourist tourist = b.getTourist_user();
                tourist.setBooking_list(null);
            }
            b.getPayment().setBooking(null);
        }

        return bookingsToReturn;
    }

    private Booking setBookingStatus(Booking b) {
        if (b.getStatus() != BookingStatusEnum.CANCELLED) {
            if (b.getStart_datetime().toLocalDate().isEqual(LocalDate.now())) {
                b.setStatus(BookingStatusEnum.ONGOING);
            } else if (b.getStart_datetime().toLocalDate().isBefore(LocalDate.now())) {
                b.setStatus(BookingStatusEnum.COMPLETED);
            } else {
                b.setStatus(BookingStatusEnum.UPCOMING);
            }
        }
        return b;
    }

    public Long createTourBooking(Long tourId, Booking newBooking) throws NotFoundException { // need to eventually add payment

        Optional<Tour> tourOptional = tourRepository.findById(tourId);

        if (tourOptional.isPresent()) {
            Tour tour = tourOptional.get();

            Payment payment = new Payment();
            payment.setPayment_amount(new BigDecimal(123));
            payment.setComission_percentage(new BigDecimal(10));
            payment.setIs_paid(true);
            paymentRepository.save(payment);

            newBooking.setTour(tour);
            newBooking.setPayment(payment);
            bookingRepository.save(newBooking);

            payment.setBooking(newBooking);
            paymentRepository.save(payment);

            return newBooking.getBooking_id();
        } else {
            throw new NotFoundException("Tour not found!");
        }
    }

    public Booking createAttractionBooking(Long attractionId, Booking booking) throws NotFoundException {

        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);

        if (attractionOptional.isPresent()) {
            Payment payment = booking.getPayment();
            paymentRepository.save(payment);

            for (BookingItem bi : booking.getBooking_item_list()) {
                bookingItemRepository.save(bi);
            }

            Attraction attraction = attractionOptional.get();
            booking.setAttraction(attraction);
            bookingRepository.save(booking);

            payment.setBooking(booking);
            paymentRepository.save(payment);

            booking.getPayment().setBooking(null);
            return booking;

        } else {
            throw new NotFoundException("Attraction not found!");
        }
    }

    public Booking createTelecomBooking(Long telecomId, Booking booking) throws NotFoundException {

        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomId);

        if (telecomOptional.isPresent()) {
            Payment payment = booking.getPayment();
            paymentRepository.save(payment);

            for (BookingItem bi : booking.getBooking_item_list()) {
                bookingItemRepository.save(bi);
            }

            Telecom telecom = telecomOptional.get();
            booking.setTelecom(telecom);
            bookingRepository.save(booking);

            payment.setBooking(booking);
            paymentRepository.save(payment);

            booking.getPayment().setBooking(null);
            return booking;

        } else {
            throw new NotFoundException("Telecom not found!");
        }
    }
}