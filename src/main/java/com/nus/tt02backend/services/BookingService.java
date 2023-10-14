package com.nus.tt02backend.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerBalanceTransaction;
import com.stripe.model.Refund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import java.util.*;
import java.util.List;
import java.math.*;
import java.time.*;

@Service
public class BookingService {

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    AttractionRepository accommodationRepository;

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
    RoomRepository roomRepository;

    @Autowired
    TelecomRepository telecomRepository;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    QrCodeRepository qrCodeRepository;

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
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Tourist findTourist(Long touristId) throws NotFoundException {
        try {
            Tourist tourist = touristRepository.getTouristByUserId(touristId);
            if (tourist != null) {
                tourist.setCart_list(null);
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
                local.setCart_list(null);
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

        List<Booking> listToReturn = new ArrayList<Booking>();
        List<Booking> bookingList = bookingRepository.findAll();

        for (Booking b : bookingList) {
            b.getPayment().setBooking(null);

            if (b.getLocal_user() != null) {
                Local local = b.getLocal_user();
                local.setBooking_list(null);

            } else if (b.getTourist_user() != null) {
                Tourist tourist = b.getTourist_user();
                tourist.setBooking_list(null);
            }

            listToReturn.add(b);
        }

        return listToReturn;
    }

    public Booking getBookingByBookingId(Long bookingId) throws NotFoundException {
        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

            if (bookingOptional.isPresent()) {
                Booking booking = bookingOptional.get();

                if (booking.getLocal_user() != null) {
                    Local local = booking.getLocal_user();
                    local.setBooking_list(null);
                    local.setCart_list(null);
                } else if (booking.getTourist_user() != null) {
                    Tourist tourist = booking.getTourist_user();
                    tourist.setCart_list(null);
                    tourist.setBooking_list(null);
                }

                if (!booking.getType().equals(BookingTypeEnum.TELECOM) &&
                        booking.getQr_code_list().isEmpty() && booking.getStatus() != BookingStatusEnum.CANCELLED) {
                    for (BookingItem bookingItem : booking.getBooking_item_list()) {
                        long[] voucherCodes = generateVoucherCodes(booking.getBooking_id(), bookingItem.getQuantity());
                        for (int i = 0; i < voucherCodes.length; i++) {
                            QrCode qrCode = new QrCode();

                            String voucherCode = Long.toString(voucherCodes[i]);
                            if (booking.getType().equals(BookingTypeEnum.ATTRACTION)) {
                                voucherCode += "AT";
                            } else if (booking.getType().equals(BookingTypeEnum.TOUR)) {
                                voucherCode += "TR";
                            } else if (booking.getType().equals(BookingTypeEnum.ACCOMMODATION)) {
                                voucherCode += "AC";
                            }

                            qrCode.setVoucher_code(voucherCode);
                            qrCode.setQr_code_link(generateAndUploadQRCode(voucherCode));
                            qrCodeRepository.save(qrCode);
                            booking.getQr_code_list().add(qrCode);
                        }
                    }
                }

                bookingRepository.save(booking);
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

        if (Duration.between(LocalDateTime.now(), booking.getStart_datetime()).toDays() >= 3) {
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put(
                    "payment_intent",
                    booking.getPayment().getPayment_id()
            );

            Refund refund = Refund.create(refundParams);

            Payment payment = booking.getPayment();

            String bookingType = String.valueOf(booking.getType());

            BigDecimal commission = payment.getPayment_amount().multiply(payment.getComission_percentage());

            BigDecimal payoutAmount = payment.getPayment_amount().subtract(commission);

            Vendor vendor = null;
            Local local = null;

            if (Objects.equals(bookingType, "TOUR")) {
                local = localRepository.findLocalByTour(booking.getTour());

                BigDecimal currentWalletBalance = local.getWallet_balance();

                local.setWallet_balance(currentWalletBalance.subtract(payoutAmount));

                String stripe_account_id = local.getStripe_account_id();

                Customer customer =
                        Customer.retrieve(stripe_account_id);

                Map<String, Object> params = new HashMap<>();
                params.put("amount", -payoutAmount.multiply(new BigDecimal("100")).intValueExact());
                params.put("currency", "sgd");
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("transaction_type", "Refunds");
                params.put("metadata", metadata);

                CustomerBalanceTransaction balanceTransaction =
                        customer.balanceTransactions().create(params);

                localRepository.save(local);
            } else {
                if (Objects.equals(bookingType, "ATTRACTION")) {
                    vendor = vendorRepository.findVendorByAttractionName(booking.getAttraction().getName());

                } else if (Objects.equals(bookingType, "TELECOM")) {
                    vendor = vendorRepository.findVendorByTelecomName(booking.getTelecom().getName());

                } else if (Objects.equals(bookingType, "ACCOMMODATION")) {
                    vendor = vendorRepository.findVendorByAccommodationName(booking.getActivity_name());
                }

                if (!(vendor == null)) {
                    BigDecimal currentWalletBalance = vendor.getWallet_balance();

                    vendor.setWallet_balance(currentWalletBalance.subtract(payoutAmount));

                    String stripe_account_id = vendor.getStripe_account_id();

                    Customer customer =
                            Customer.retrieve(stripe_account_id);

                    Map<String, Object> params = new HashMap<>();
                    params.put("amount", -payoutAmount.multiply(new BigDecimal("100")).intValueExact());
                    params.put("currency", "sgd");
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("transaction_type", "Refunds");
                    params.put("metadata", metadata);

                    CustomerBalanceTransaction balanceTransaction =
                            customer.balanceTransactions().create(params);

                    vendorRepository.save(vendor);
                }
            }



        }

        List<QrCode> qrCodes = new ArrayList<QrCode>();
        qrCodes.addAll(booking.getQr_code_list());

        for (QrCode qrCode : qrCodes) {
            amazonS3.deleteObject(new DeleteObjectRequest("tt02", "bookings/" + qrCode.getVoucher_code() + ".png"));
            booking.getQr_code_list().remove(qrCode);
            qrCodeRepository.deleteById(qrCode.getQr_code_id());
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
        List<Booking> bookingList = bookingRepository.findAll();

        for (Booking b : bookingList) {
            if (!vendor.getAttraction_list().isEmpty()) {
                // attraction
                for (Attraction a : vendor.getAttraction_list()) {
                    if (b.getAttraction() != null && b.getAttraction().getAttraction_id() == a.getAttraction_id()) {
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
                        Booking temp = this.setBookingStatus(b);
                        bookingRepository.save(temp);
                        bookingsToReturn.add(temp);
                    }
                }
            }

            if (!vendor.getAccommodation_list().isEmpty()) {
                for (Accommodation accommodation : vendor.getAccommodation_list()) {
                    if (!accommodation.getRoom_list().isEmpty()) {
                        for (Room room: accommodation.getRoom_list())
                        if (b.getRoom() != null && Objects.equals(b.getRoom().getRoom_id(), room.getRoom_id())) {
                            Booking temp = this.setBookingStatus(b);
                            bookingRepository.save(temp);
                            bookingsToReturn.add(temp);
                        }
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

    public BufferedImage generateQRCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage qrCodeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = (Graphics2D) qrCodeImage.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            return qrCodeImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateAndUploadQRCode(String text) {
        try {
            BufferedImage qrCodeImage = generateQRCode(text, 200, 200);

            if (qrCodeImage != null) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrCodeImage, "png", baos);
                    byte[] qrCodeBytes = baos.toByteArray();
                    String fileName = "bookings/" + text + ".png";
                    InputStream inputStream = new ByteArrayInputStream(qrCodeBytes);

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(qrCodeBytes.length);

                    amazonS3.putObject(new PutObjectRequest("tt02", fileName, inputStream, metadata));

                    return amazonS3.getUrl("tt02", fileName).toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long[] generateVoucherCodes(long bookingId, int numTickets) {
        long[] uniqueNumbers = new long[numTickets];
        long baseNumber = bookingId * 1000000L;

        for (int i = 0; i < numTickets; i++) {
            uniqueNumbers[i] = baseNumber + i + 1;
        }

        return uniqueNumbers;
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

    public Booking createRoomBooking(Long roomId, Booking booking) throws NotFoundException {

        Optional<Room> roomOptional = roomRepository.findById(roomId);

        if (roomOptional.isPresent()) {
            Payment payment = booking.getPayment();
            paymentRepository.save(payment);

            for (BookingItem bi : booking.getBooking_item_list()) {
                bookingItemRepository.save(bi);
            }

            Room room = roomOptional.get();
            booking.setRoom(room);
            bookingRepository.save(booking);

            payment.setBooking(booking);
            paymentRepository.save(payment);

            booking.getPayment().setBooking(null);
            bookingRepository.save(booking);
            return booking;

        } else {
            throw new NotFoundException("Accommodation not found!");
        }
    }
}