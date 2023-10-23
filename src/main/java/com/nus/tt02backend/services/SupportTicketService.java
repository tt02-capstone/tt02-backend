package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class SupportTicketService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    InternalStaffRepository internalStaffRepository;

    @Autowired
    SupportTicketRepository supportTicketRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    TourRepository tourRepository;

    @Autowired
    TelecomRepository telecomRepository;

    @Autowired
    DealRepository dealRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    BookingService bookingService;

    @Autowired
    AccommodationService accommodationService;

    @Autowired
    JavaMailSender javaMailSender;

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

    public List<SupportTicket> getAllOutgoingSupportTicketsByVendorStaff(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById(vendorStaffId)
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));
        vendorStaff.getVendor().setVendor_staff_list(null);
        List<SupportTicket> supportTickets = vendorStaff.getOutgoing_support_ticket_list();
        for (SupportTicket s : supportTickets) {
            if (!s.getReply_list().isEmpty()) {
                List<Reply> replyList = s.getReply_list();
                for (Reply r : replyList) {
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                    r.setInternal_staff_user(null);
                    r.setTourist_user(null);
                    r.setLocal_user(null);
                }
            }
            if (s.getBooking() != null) {
                s.getBooking().setPayment(null);
                s.getBooking().setLocal_user(null);
                s.getBooking().setTourist_user(null);
            }
        }
        return supportTickets;
    }



    public List<SupportTicket> getAllIncomingSupportTicketsByVendorStaff(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById(vendorStaffId)
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));
        vendorStaff.getVendor().setVendor_staff_list(null);
        List<SupportTicket> supportTickets = vendorStaff.getIncoming_support_ticket_list();
        for (SupportTicket s : supportTickets) {
            if (!s.getReply_list().isEmpty()) {
                List<Reply> replyList = s.getReply_list();
                for (Reply r : replyList) {
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                    r.setInternal_staff_user(null);
                    r.setTourist_user(null);
                    r.setLocal_user(null);
                }
            }
            if (s.getBooking() != null) {
                s.getBooking().setPayment(null);
                s.getBooking().setLocal_user(null);
                s.getBooking().setTourist_user(null);
            }
        }
        return supportTickets;
    }

    public SupportTicket getSupportTicket(Long supportTicketId) throws NotFoundException {
        try {
            Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
            if (supportTicketOptional.isPresent()) {

                SupportTicket supportTicket = supportTicketOptional.get();
                if (!supportTicket.getReply_list().isEmpty()) {
                    List<Reply> replyList = supportTicket.getReply_list();
                    for (Reply r : replyList) {
                        if (r.getVendor_staff_user() != null) {
                            r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                            r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                        } else if (r.getInternal_staff_user() != null) {
                            r.getInternal_staff_user().setSupport_ticket_list(null);
                        } else if (r.getTourist_user() != null) {
                            r.getTourist_user().setSupport_ticket_list(null);
                            r.getTourist_user().setBooking_list(null);
                            r.getTourist_user().setPost_list(null);
                            r.getTourist_user().setComment_list(null);
                            r.getTourist_user().setCart_list(null);
                            r.getTourist_user().setTour_type_list(null);
                        } else if (r.getLocal_user() != null) {
                            r.getLocal_user().setSupport_ticket_list(null);
                            r.getLocal_user().setBooking_list(null);
                            r.getLocal_user().setPost_list(null);
                            r.getLocal_user().setComment_list(null);
                            r.getLocal_user().setCart_list(null);
                            r.getLocal_user().setTour_type_list(null);
                        }
                    }
                }

                if (supportTicket.getBooking() != null) {
                    supportTicket.getBooking().getPayment().setBooking(null);
                    supportTicket.getBooking().setTourist_user(null);
                    supportTicket.getBooking().setLocal_user(null);
                }

                return supportTicket;
            } else {
                throw new NotFoundException("Support Ticket not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<SupportTicket> getAllSupportTicketsByUser(Long userId) throws NotFoundException, BadRequestException {
        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);
        List<SupportTicket> supportTickets = new ArrayList<SupportTicket>();

        if (currentUser.getUser_type().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            supportTickets = tourist.getSupport_ticket_list();
        } else if (currentUser.getUser_type().equals(localType)) {
            Local local = findLocal(userId);
            supportTickets = local.getSupport_ticket_list();
        } else {
            throw new BadRequestException("Current user type is not tourist or local");
        }

        for (SupportTicket s : supportTickets) {
            if (!s.getReply_list().isEmpty()) {
                List<Reply> replyList = s.getReply_list();
                for (Reply r : replyList) {
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                    r.setInternal_staff_user(null);
                    r.setTourist_user(null);
                    r.setLocal_user(null);
                }
            }
            if (s.getBooking() != null) {
                s.getBooking().setPayment(null);
                s.getBooking().setLocal_user(null);
                s.getBooking().setTourist_user(null);
            }
        }

        return supportTickets;
    }

    public List<SupportTicket> getAllSupportTicketsByAdmin(Long adminId) throws NotFoundException, BadRequestException {

        InternalStaff internalStaff = internalStaffRepository.findById(adminId).orElseThrow(() -> new NotFoundException("Admin not found"));

        List<SupportTicket> supportTickets = internalStaff.getSupport_ticket_list();

        for (SupportTicket s : supportTickets) {
            if (!s.getReply_list().isEmpty()) {
                List<Reply> replyList = s.getReply_list();
                for (Reply r : replyList) {
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                    r.setInternal_staff_user(null);
                    r.setTourist_user(null);
                    r.setLocal_user(null);
                }
            }
            if (s.getBooking() != null) {
                s.getBooking().setPayment(null);
                s.getBooking().setLocal_user(null);
                s.getBooking().setTourist_user(null);
            }
        }

        return supportTickets;
    }

    public List<SupportTicket> getAllSupportTickets() {

        List<SupportTicket> supportTicketList = supportTicketRepository.findAll();

        for (SupportTicket s : supportTicketList) {
            if (!s.getReply_list().isEmpty()) {
                List<Reply> replyList = s.getReply_list();
                for (Reply r : replyList) {
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    if (r.getVendor_staff_user() != null) r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                    r.setInternal_staff_user(null);
                    r.setTourist_user(null);
                    r.setLocal_user(null);
                }
            }
            if (s.getBooking() != null) {
//                s.getBooking().setPayment(null);
                s.getBooking().setLocal_user(null);
//                s.getBooking().setTourist_user(null);
            }
        }

        return supportTicketList;
    }

    public SupportTicket createSupportTicketToAdmin(Long userId, SupportTicket supportTicketToCreate) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        supportTicketToCreate.setCreated_time(LocalDateTime.now());
        supportTicketToCreate.setUpdated_time(LocalDateTime.now());
        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setTicket_type(SupportTicketTypeEnum.ADMIN);
        supportTicketToCreate.setSubmitted_user(user.getUser_type());
        supportTicketToCreate.setSubmitted_user_name(user.getName());
        supportTicketToCreate.setSubmitted_user_id(user.getUser_id());
        supportTicketToCreate.setReply_list(new ArrayList<>());

        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);

        UserTypeEnum userType;
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.getSupport_ticket_list().add(supportTicket);
            touristRepository.save(tourist);

        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.getSupport_ticket_list().add(supportTicket);
            localRepository.save(local);

        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            vendorStaff.getOutgoing_support_ticket_list().add(supportTicket);
            vendorStaffRepository.save(vendorStaff);

        }

        List<InternalStaff> internalStaffList = internalStaffRepository.findAll();
        for (InternalStaff i : internalStaffList) {
            if (i.getRole().equals(InternalRoleEnum.ADMIN) || i.getRole().equals(InternalRoleEnum.SUPPORT)) {
                List<SupportTicket> supportTicketList = i.getSupport_ticket_list();
                supportTicketList.add(supportTicket);
                i.setSupport_ticket_list(supportTicketList);
                internalStaffRepository.save(i);
            }
        }

        try {
            String subject = "[WithinSG] Support Ticket To Admin Created";
            String content = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<p style='color: #333; font-size: 16px;'>Dear " + user.getName() + ",</p>"
                    + "<p style='color: #333; font-size: 16px;'>You have submitted a support ticket to WithinSG's Internal Staff.</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Id:</strong> " + supportTicket.getSupport_ticket_id().toString() + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Category:</strong> " + convertToTitleCase(supportTicket.getTicket_category().toString()) + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Message Contents:</strong> <em>" + supportTicket.getDescription() + "</em></p>"
                    + "<p style='color: #333; font-size: 16px;'>Kind Regards,<br> WithinSG</p>"
                    + "</body></html>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return supportTicket;
    }

    public List<VendorStaff> retrieveAllVendorStaff() {
        List<VendorStaff> vendorStaffList = vendorStaffRepository.findAll();

        for (VendorStaff i : vendorStaffList) {
            i.getVendor().setVendor_staff_list(null);
        }

        return vendorStaffList;
    }

    public SupportTicket createSupportTicketToVendor(Long userId, Long activityId, SupportTicket supportTicketToCreate) throws BadRequestException, NotFoundException {

        User user = findUser(userId);

        supportTicketToCreate.setCreated_time(LocalDateTime.now());
        supportTicketToCreate.setUpdated_time(LocalDateTime.now());
        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setTicket_type(SupportTicketTypeEnum.VENDOR);
        supportTicketToCreate.setSubmitted_user(user.getUser_type());
        supportTicketToCreate.setSubmitted_user_id(user.getUser_id());
        supportTicketToCreate.setSubmitted_user_name(user.getName());
        supportTicketToCreate.setReply_list(new ArrayList<>());

        List<VendorStaff> vendorStaffList = new ArrayList<>();
        String activityName = null;

        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);

        System.out.println("Support Ticket Ticket Category" + supportTicket.getTicket_category());

        if (supportTicket.getTicket_category().equals(SupportTicketCategoryEnum.ACCOMMODATION)) {
            Optional<Accommodation> accommodationOptional = accommodationRepository.findById(activityId);
            if (accommodationOptional.isEmpty()) {
                throw new BadRequestException("Accommodation does not exist!");
            }
            Accommodation accommodation = accommodationOptional.get();
            supportTicket.setAccommodation(accommodation);
            vendorStaffList = getVendorByAccommodation(activityId);
            activityName = "Accommodation: " + accommodation.getName();

        } else if (supportTicket.getTicket_category().equals(SupportTicketCategoryEnum.ATTRACTION)) {
            System.out.println("Entered the attraction loop");
            System.out.println("activity Id" + activityId);

            Optional<Attraction> attractionOptional = attractionRepository.findById(activityId);
            if (attractionOptional.isEmpty()) {
                throw new BadRequestException("Attraction does not exist!");
            }
            Attraction attraction = attractionOptional.get();
            supportTicket.setAttraction(attraction);
            vendorStaffList = getVendorByAttraction(activityId);
            activityName = "Attraction: " + attraction.getName();
        } else if (supportTicket.getTicket_category().equals(SupportTicketCategoryEnum.RESTAURANT)) {
            Optional<Restaurant> restaurantOptional = restaurantRepository.findById(activityId);
            if (restaurantOptional.isEmpty()) {
                throw new BadRequestException("Restaurant does not exist!");
            }
            Restaurant restaurant = restaurantOptional.get();
            supportTicket.setRestaurant(restaurant);
            vendorStaffList = getVendorByRestaurant(activityId);
            activityName = "Restaurant: " + restaurant.getName();
        } else if (supportTicket.getTicket_category().equals(SupportTicketCategoryEnum.TELECOM)) {
            Optional<Telecom> telecomOptional = telecomRepository.findById(activityId);
            if (telecomOptional.isEmpty()) {
                throw new BadRequestException("Telecom package does not exist!");
            }
            Telecom telecom = telecomOptional.get();
            supportTicket.setTelecom(telecom);
            vendorStaffList = getVendorByTelecom(activityId);
            activityName = "Telecom: " + telecom.getName();
        } else if (supportTicket.getTicket_category().equals(SupportTicketCategoryEnum.DEAL)) {
            Optional<Deal> dealOptional = dealRepository.findById(activityId);
            if (dealOptional.isEmpty()) {
                throw new BadRequestException("Deal does not exist!");
            }
            Deal deal = dealOptional.get();
            supportTicket.setDeal(deal);
            vendorStaffList = getVendorByDeal(activityId);
            activityName = "Deal: " + deal.getPromo_code();
        }

        supportTicketRepository.save(supportTicket);

        for (VendorStaff v : vendorStaffList) {
            List<SupportTicket> incomingSupportTicketList = v.getIncoming_support_ticket_list();
            incomingSupportTicketList.add(supportTicket);
            v.setIncoming_support_ticket_list(incomingSupportTicketList);
            vendorStaffRepository.save(v);
        }

        UserTypeEnum userType;
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.getSupport_ticket_list().add(supportTicket);
            touristRepository.save(tourist);
        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.getSupport_ticket_list().add(supportTicket);
            localRepository.save(local);
        }

        try {
            String subject = "[WithinSG] Support Ticket To Vendor Created";
            String content = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<p style='color: #333; font-size: 16px;'>Dear " + user.getName() + ",</p>"
                    + "<p style='color: #333; font-size: 16px;'>You have submitted a support ticket to Vendor - " + activityName + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Id:</strong> " + supportTicket.getSupport_ticket_id().toString() + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Category:</strong> " + convertToTitleCase(supportTicket.getTicket_category().toString()) + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Message Contents:</strong> <em>" + supportTicket.getDescription() + "</em></p>"
                    + "<p style='color: #333; font-size: 16px;'>Kind Regards,<br> WithinSG</p>"
                    + "</body></html>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return supportTicket;
    }

    // FOR ATTRACTION SUPPORT TICKET

    public List<Attraction> retrieveAllAttractionsByVendor(Vendor vendor) throws NotFoundException {

        List<Attraction> listToReturn = new ArrayList<>();

        if (!vendor.getAttraction_list().isEmpty()) {
            listToReturn = vendor.getAttraction_list();
        }

        return listToReturn;
    }

    public List<VendorStaff> getVendorByAttraction(Long attractionId) throws BadRequestException, NotFoundException {
        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);
        if (attractionOptional.isEmpty()) {
            throw new BadRequestException("Attraction does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();
        List<VendorStaff> listToReturn = new ArrayList<>();

        for (VendorStaff v : vendorStaffList) {
            List<Attraction> vendorAttractionList = retrieveAllAttractionsByVendor(v.getVendor());

            for (Attraction a : vendorAttractionList) {
                if (a.getAttraction_id().equals(attractionId)) {
                    listToReturn.add(v);
                }
            }
        }
        return listToReturn;
    }

    // FOR ACCOMMODATION SUPPORT TICKET

    public List<Accommodation> retrieveAllAccommodationsByVendor(Vendor vendor) throws NotFoundException {

        List<Accommodation> listToReturn = new ArrayList<>();

        if (!vendor.getAccommodation_list().isEmpty()) {
            listToReturn = vendor.getAccommodation_list();
        }

        return listToReturn;

    }

    public List<VendorStaff> getVendorByAccommodation(Long accommodationId) throws BadRequestException, NotFoundException {

        Optional<Accommodation> accommodationOptional = accommodationRepository.findById(accommodationId);
        if (accommodationOptional.isEmpty()) {
            throw new BadRequestException("Accommodation does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();
        List<VendorStaff> listToReturn = new ArrayList<>();

        for (VendorStaff v : vendorStaffList) {
            List<Accommodation> vendorAccommodationList = retrieveAllAccommodationsByVendor(v.getVendor());

            for (Accommodation a : vendorAccommodationList) {
                if (a.getAccommodation_id().equals(accommodationId)) {
                    listToReturn.add(v);
                }
            }
        }
        return listToReturn;
    }

    // FOR RESTAURANT SUPPORT TICKET

    public List<Restaurant> retrieveAllRestaurantsByVendor(Vendor vendor) throws NotFoundException {

        List<Restaurant> listToReturn = new ArrayList<>();

        if (!vendor.getRestaurant_list().isEmpty()) {
            listToReturn = vendor.getRestaurant_list();
        }

        return listToReturn;
    }

    public List<VendorStaff> getVendorByRestaurant(Long restaurantId) throws BadRequestException, NotFoundException {

        Optional<Restaurant> restaurantOptional = restaurantRepository.findById(restaurantId);
        if (restaurantOptional.isEmpty()) {
            throw new BadRequestException("Restaurant does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();
        List<VendorStaff> listToReturn = new ArrayList<>();

        for (VendorStaff v : vendorStaffList) {
            List<Restaurant> vendorRestaurantList = retrieveAllRestaurantsByVendor(v.getVendor());

            for (Restaurant r : vendorRestaurantList) {
                if (r.getRestaurant_id().equals(restaurantId)) {
                    listToReturn.add(v);
                }
            }
        }
        return listToReturn;
    }

    // FOR TELECOM SUPPORT TICKET

    public List<Telecom> retrieveAllTelecomsByVendor(Vendor vendor) throws NotFoundException {

        List<Telecom> listToReturn = new ArrayList<>();

        if (!vendor.getTelecom_list().isEmpty()) {
            listToReturn = vendor.getTelecom_list();
        }

        return listToReturn;

    }

    public List<VendorStaff> getVendorByTelecom(Long telecomId) throws BadRequestException, NotFoundException {

        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomId);
        if (telecomOptional.isEmpty()) {
            throw new BadRequestException("Telecom package does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();
        List<VendorStaff> listToReturn = new ArrayList<>();

        for (VendorStaff v : vendorStaffList) {
            List<Telecom> vendorTelecomList = retrieveAllTelecomsByVendor(v.getVendor());

            for (Telecom t : vendorTelecomList) {
                if (t.getTelecom_id().equals(telecomId)) {
                    listToReturn.add(v);
                }
            }
        }
        return listToReturn;
    }

    // FOR DEAL SUPPORT TICKET

    public List<Deal> retrieveAllDealsByVendor(Vendor vendor) throws NotFoundException {

        List<Deal> listToReturn = new ArrayList<>();

        if (!vendor.getDeals_list().isEmpty()) {
            listToReturn = vendor.getDeals_list();
        }

        return listToReturn;
    }

    public List<VendorStaff> getVendorByDeal(Long dealId) throws BadRequestException, NotFoundException {

        Optional<Deal> dealOptional = dealRepository.findById(dealId);
        if (dealOptional.isEmpty()) {
            throw new BadRequestException("Deal does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();
        List<VendorStaff> listToReturn = new ArrayList<>();

        for (VendorStaff v : vendorStaffList) {
            List<Deal> vendorDealList = retrieveAllDealsByVendor(v.getVendor());

            for (Deal d : vendorDealList) {
                if (d.getDeal_id().equals(dealId)) {
                    listToReturn.add(v);
                }
            }
        }
        return listToReturn;
    }

    public Booking getBookingByBookingId(Long bookingId) throws NotFoundException {
        try {
            Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

            if (bookingOptional.isPresent()) {
                Booking booking = bookingOptional.get();
                return booking;
            } else {
                throw new NotFoundException("Booking not found");
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

    public SupportTicket createSupportTicketForBooking(Long userId, Long bookingId, SupportTicket supportTicketToCreate) throws BadRequestException, NotFoundException {

        User user = findUser(userId);

        supportTicketToCreate.setCreated_time(LocalDateTime.now());
        supportTicketToCreate.setUpdated_time(LocalDateTime.now());
        supportTicketToCreate.setSubmitted_user(user.getUser_type());
        supportTicketToCreate.setSubmitted_user_id(user.getUser_id());
        supportTicketToCreate.setSubmitted_user_name(user.getName());
        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setReply_list(new ArrayList<>());

        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            throw new BadRequestException("Booking does not exist!");
        }
        Booking booking = bookingOptional.get();
        supportTicketToCreate.setBooking(booking);
        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);

        List<VendorStaff> vendorStaffList = new ArrayList<>();
        String activityName = null;
        if (booking.getAttraction() != null) {
            Attraction attraction = booking.getAttraction();
            vendorStaffList = getVendorByAttraction(attraction.getAttraction_id());
            activityName = " with Attraction: " + attraction.getName();
        } else if (booking.getRoom() != null) {
            Room room = booking.getRoom();
            Accommodation accommodation = accommodationService.retrieveAccommodationByRoom(room.getRoom_id());
            vendorStaffList = getVendorByAccommodation(accommodation.getAccommodation_id());
            activityName = " with Accommodation: " + accommodation.getName();
        } else if (booking.getTelecom() != null) {
            Telecom telecom = booking.getTelecom();
            vendorStaffList = getVendorByTelecom(telecom.getTelecom_id());
            activityName = " with Telecom: " + telecom.getName();
        } else if (booking.getDeal() != null) {
            Deal deal = booking.getDeal();
            vendorStaffList = getVendorByDeal(deal.getDeal_id());
            activityName = " with Deal: " + deal.getPromo_code();
        }
        // tour if adding

        if (supportTicket.getTicket_type().equals(SupportTicketTypeEnum.ADMIN)) {
            List<InternalStaff> internalStaffList = internalStaffRepository.findAll();
            for (InternalStaff i : internalStaffList) {
                if (i.getRole().equals(InternalRoleEnum.ADMIN) || i.getRole().equals(InternalRoleEnum.SUPPORT)) {
                    List<SupportTicket> supportTicketList = i.getSupport_ticket_list();
                    supportTicketList.add(supportTicket);
                    i.setSupport_ticket_list(supportTicketList);
                    internalStaffRepository.save(i);
                }
            }
        } else if (supportTicket.getTicket_type().equals(SupportTicketTypeEnum.VENDOR)) {
            for (VendorStaff v : vendorStaffList) {
                List<SupportTicket> supportTicketList = v.getIncoming_support_ticket_list();
                supportTicketList.add(supportTicket);
                v.setIncoming_support_ticket_list(supportTicketList);
                vendorStaffRepository.save(v);
            }
        }

        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            tourist.getSupport_ticket_list().add(supportTicket);
            touristRepository.save(tourist);

        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            local.getSupport_ticket_list().add(supportTicket);
            localRepository.save(local);

        }

        supportTicket.getBooking().getPayment().setBooking(null);
        supportTicket.getBooking().setLocal_user(null);
        supportTicket.getBooking().setTourist_user(null);

        try {
            String subject = "[WithinSG] Support Ticket To " +  convertToTitleCase(supportTicket.getTicket_type().toString()) + " Created";
            String content = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<p style='color: #333; font-size: 16px;'>Dear " + user.getName() + ",</p>"
                    + "<p style='color: #333; font-size: 16px;'>You have submitted a support ticket to " + convertToTitleCase(supportTicket.getTicket_type().toString()) +
                    " regarding your Booking #" + supportTicket.getBooking().getBooking_id() + activityName + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Id:</strong> " + supportTicket.getSupport_ticket_id().toString() + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Ticket Category:</strong> " + convertToTitleCase(supportTicket.getTicket_category().toString()) + "</p>"
                    + "<p style='color: #333; font-size: 16px;'><strong>Message Contents:</strong> <em>" + supportTicket.getDescription() + "</em></p>"
                    + "<p style='color: #333; font-size: 16px;'>Kind Regards,<br> WithinSG</p>"
                    + "</body></html>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return supportTicket;
    }

    public SupportTicket updateSupportTicketStatus(Long supportTicketId) throws NotFoundException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);

        if (supportTicketOptional.isPresent()) {

            SupportTicket supportTicket = supportTicketOptional.get();
            Boolean currentStatus = supportTicket.getIs_resolved();
            supportTicket.setIs_resolved(!currentStatus);
            supportTicketRepository.save(supportTicket);

            return supportTicket;

        } else {
            throw new NotFoundException("Support Ticket Not Found!");
        }
    }

    public SupportTicket updateSupportTicket(Long supportTicketId, SupportTicket supportTicketToUpdate) throws NotFoundException, BadRequestException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support Ticket does not exist!");
        }

        SupportTicket supportTicket = supportTicketOptional.get();

        if (!supportTicket.getReply_list().isEmpty()) {
            throw new BadRequestException("Unable to edit support ticket as there are existing replies.");
        }

        supportTicket.setUpdated_time(LocalDateTime.now());
        supportTicket.setDescription(supportTicketToUpdate.getDescription());

        supportTicketRepository.save(supportTicket);

        return supportTicket;
    }

    public void deleteSupportTicket(Long supportTicketId) throws NotFoundException, BadRequestException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support Ticket does not exist!");
        }

        SupportTicket supportTicket = supportTicketOptional.get();

        if (!supportTicket.getReply_list().isEmpty()) {
            throw new BadRequestException("Unable to delete support ticket as there are existing replies.");
        } else {

            List<Tourist> touristList = touristRepository.findAll();
            for (Tourist t : touristList) {
                List<SupportTicket> touristSupportTicketList = t.getSupport_ticket_list();
                for (SupportTicket s : touristSupportTicketList) {
                    if (s.getSupport_ticket_id().equals(supportTicketId)) {
                        touristSupportTicketList.remove(s);
                        t.setSupport_ticket_list(touristSupportTicketList);
                        touristRepository.save(t);
                        break;
                    }
                }
            }

            List<Local> localList = localRepository.findAll();
            for (Local l : localList) {
                List<SupportTicket> localSupportTicketList = l.getSupport_ticket_list();
                for (SupportTicket s : localSupportTicketList) {
                    if (s.getSupport_ticket_id().equals(supportTicketId)) {
                        localSupportTicketList.remove(s);
                        l.setSupport_ticket_list(localSupportTicketList);
                        localRepository.save(l);
                        break;
                    }
                }
            }

            List<VendorStaff> vendorStaffList = vendorStaffRepository.findAll();
            for (VendorStaff v : vendorStaffList) {
                List<SupportTicket> vendorStaffIncomingSupportTicketList = v.getIncoming_support_ticket_list();
                List<SupportTicket> vendorStaffOutgoingSupportTicketList = v.getOutgoing_support_ticket_list();

                for (SupportTicket s : vendorStaffIncomingSupportTicketList) {
                    if (s.getSupport_ticket_id().equals(supportTicketId)) {
                        vendorStaffIncomingSupportTicketList.remove(s);
                        v.setIncoming_support_ticket_list(vendorStaffIncomingSupportTicketList);
                        vendorStaffRepository.save(v);
                        break;
                    }
                }

                for (SupportTicket s : vendorStaffOutgoingSupportTicketList) {
                    if (s.getSupport_ticket_id().equals(supportTicketId)) {
                        vendorStaffOutgoingSupportTicketList.remove(s);
                        v.setOutgoing_support_ticket_list(vendorStaffOutgoingSupportTicketList);
                        vendorStaffRepository.save(v);
                        break;
                    }
                }
            }

            List<InternalStaff> internalStaffList = internalStaffRepository.findAll();
            for (InternalStaff i : internalStaffList) {
                List<SupportTicket> internalStaffSupportTicketList = i.getSupport_ticket_list();
                for (SupportTicket s : internalStaffSupportTicketList) {
                    if (s.getSupport_ticket_id().equals(supportTicketId)) {
                        internalStaffSupportTicketList.remove(s);
                        i.setSupport_ticket_list(internalStaffSupportTicketList);
                        internalStaffRepository.save(i);
                        break;
                    }
                }
            }

            supportTicketRepository.deleteById(supportTicketId);

        }

    }

    public String getUserAvatarImage(Long userId) throws BadRequestException {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }
        User user = userOptional.get();

        return user.getProfile_pic();
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public static String convertToTitleCase(String input) {
        // Split the input string by underscore
        String[] words = input.split("_");

        // Initialize a StringBuilder to store the result
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            // Capitalize the first letter of each word
            String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();

            // Append the capitalized word to the result with a space
            result.append(capitalizedWord).append(" ");
        }

        // Remove the trailing space and return the result
        return result.toString().trim();
    }

}