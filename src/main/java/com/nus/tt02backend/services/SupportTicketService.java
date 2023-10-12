package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.SupportTicketCategoryEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
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

    public List<SupportTicket> getAllSupportTickets() {
        return supportTicketRepository.findAll();
    }

    public SupportTicket getSupportTicket(Long supportTicketId) throws NotFoundException {
        try {
            Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
            if (supportTicketOptional.isPresent()) {
                return supportTicketOptional.get();
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

        return supportTickets;
    }

    public SupportTicket createSupportTicketToAdmin(Long userId, SupportTicket supportTicketToCreate) throws BadRequestException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        supportTicketToCreate.setCreated_time(LocalDateTime.now());
        supportTicketToCreate.setUpdated_time(LocalDateTime.now());
        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setReply_list(new ArrayList<>());

        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);

        User user = userOptional.get();
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

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        supportTicketToCreate.setCreated_time(LocalDateTime.now());
        supportTicketToCreate.setUpdated_time(LocalDateTime.now());
        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setReply_list(new ArrayList<>());

        VendorStaff vendorStaff = null;

        if (supportTicketToCreate.getTicket_category().equals(SupportTicketCategoryEnum.ACCOMMODATION)) {
            Optional<Accommodation> accommodationOptional = accommodationRepository.findById(activityId);
            if (accommodationOptional.isEmpty()) {
                throw new BadRequestException("Accommodation does not exist!");
            }
            Accommodation accommodation = accommodationOptional.get();
            supportTicketToCreate.setAccommodation(accommodation);

            vendorStaff = getVendorByAccommodation(activityId);

        } else if (supportTicketToCreate.getTicket_category().equals(SupportTicketCategoryEnum.ATTRACTION)) {
            Optional<Attraction> attractionOptional = attractionRepository.findById(activityId);
            if (attractionOptional.isEmpty()) {
                throw new BadRequestException("Attraction does not exist!");
            }
            Attraction attraction = attractionOptional.get();
            supportTicketToCreate.setAttraction(attraction);

            vendorStaff = getVendorByAttraction(activityId);
        } else if (supportTicketToCreate.getTicket_category().equals(SupportTicketCategoryEnum.RESTAURANT)) {
            Optional<Restaurant> restaurantOptional = restaurantRepository.findById(activityId);
            if (restaurantOptional.isEmpty()) {
                throw new BadRequestException("Restaurant does not exist!");
            }
            Restaurant restaurant = restaurantOptional.get();
            supportTicketToCreate.setRestaurant(restaurant);

            vendorStaff = getVendorByRestaurant(activityId);
        } else if (supportTicketToCreate.getTicket_category().equals(SupportTicketCategoryEnum.TELECOM)) {
            Optional<Telecom> telecomOptional = telecomRepository.findById(activityId);
            if (telecomOptional.isEmpty()) {
                throw new BadRequestException("Telecom package does not exist!");
            }
            Telecom telecom = telecomOptional.get();
            supportTicketToCreate.setTelecom(telecom);

            vendorStaff = getVendorByTelecom(activityId);
        } else if (supportTicketToCreate.getTicket_category().equals(SupportTicketCategoryEnum.DEAL)) {
            Optional<Deal> dealOptional = dealRepository.findById(activityId);
            if (dealOptional.isEmpty()) {
                throw new BadRequestException("Deal does not exist!");
            }
            Deal deal = dealOptional.get();
            supportTicketToCreate.setDeal(deal);

            vendorStaff = getVendorByDeal(activityId);
        }

        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);
        vendorStaff.getIncoming_support_ticket_list().add(supportTicket);
        vendorStaffRepository.save(vendorStaff);

        User user = userOptional.get();
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

        // to edit
//        try {
//            String subject = "[WithinSG] Support Ticket Sent";
//            String content = "<p>Dear " + user.getName() + ",</p>" +
//                    "<p>You have submitted a support ticket to </p>" +
//                    "<p>Message Contents: </p>" +
//                    "<p>Kind Regards,<br> WithinSG</p>";
//            sendEmail(user.getEmail(), subject, content);
//        } catch (MessagingException ex) {
//            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
//        }

        return supportTicket;
    }

    // FOR ATTRACTION SUPPORT TICKET

    public List<Attraction> retrieveAllAttractionsByVendor(Vendor vendor) throws NotFoundException {

        if (!vendor.getAttraction_list().isEmpty()) {
            return vendor.getAttraction_list();
        } else {
            throw new NotFoundException("Attractions not found!");
        }
    }

    public VendorStaff getVendorByAttraction(Long attractionId) throws BadRequestException, NotFoundException {

        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);
        if (attractionOptional.isEmpty()) {
            throw new BadRequestException("Attraction does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();

        for (VendorStaff v : vendorStaffList) {
            List<Attraction> vendorAttractionList = retrieveAllAttractionsByVendor(v.getVendor());

            for (Attraction a : vendorAttractionList) {
                if (a.getAttraction_id().equals(attractionId)) {
                    return v;
                }
            }
        }
        throw new NotFoundException("Vendor not found!");
    }

    // FOR ACCOMMODATION SUPPORT TICKET

    public List<Accommodation> retrieveAllAccommodationsByVendor(Vendor vendor) throws NotFoundException {

        if (!vendor.getAccommodation_list().isEmpty()) {
            return vendor.getAccommodation_list();
        } else {
            throw new NotFoundException("Accommodations not found!");
        }
    }

    public VendorStaff getVendorByAccommodation(Long accommodationId) throws BadRequestException, NotFoundException {

        Optional<Accommodation> accommodationOptional = accommodationRepository.findById(accommodationId);
        if (accommodationOptional.isEmpty()) {
            throw new BadRequestException("Accommodation does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();

        for (VendorStaff v : vendorStaffList) {
            List<Accommodation> vendorAccommodationList = retrieveAllAccommodationsByVendor(v.getVendor());

            for (Accommodation a : vendorAccommodationList) {
                if (a.getAccommodation_id().equals(accommodationId)) {
                    return v;
                }
            }
        }
        throw new NotFoundException("Vendor not found!");
    }

    // FOR RESTAURANT SUPPORT TICKET

    public List<Restaurant> retrieveAllRestaurantsByVendor(Vendor vendor) throws NotFoundException {

        if (!vendor.getRestaurant_list().isEmpty()) {
            return vendor.getRestaurant_list();
        } else {
            throw new NotFoundException("Restaurants not found!");
        }
    }

    public VendorStaff getVendorByRestaurant(Long restaurantId) throws BadRequestException, NotFoundException {

        Optional<Restaurant> restaurantOptional = restaurantRepository.findById(restaurantId);
        if (restaurantOptional.isEmpty()) {
            throw new BadRequestException("Restaurant does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();

        for (VendorStaff v : vendorStaffList) {
            List<Restaurant> vendorRestaurantList = retrieveAllRestaurantsByVendor(v.getVendor());

            for (Restaurant r : vendorRestaurantList) {
                if (r.getRestaurant_id().equals(restaurantId)) {
                    return v;
                }
            }
        }
        throw new NotFoundException("Vendor not found!");
    }

    // FOR TELECOM SUPPORT TICKET

    public List<Telecom> retrieveAllTelecomsByVendor(Vendor vendor) throws NotFoundException {

        if (!vendor.getTelecom_list().isEmpty()) {
            return vendor.getTelecom_list();
        } else {
            throw new NotFoundException("Telecom packages not found!");
        }
    }

    public VendorStaff getVendorByTelecom(Long telecomId) throws BadRequestException, NotFoundException {

        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomId);
        if (telecomOptional.isEmpty()) {
            throw new BadRequestException("Telecom package does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();

        for (VendorStaff v : vendorStaffList) {
            List<Telecom> vendorTelecomList = retrieveAllTelecomsByVendor(v.getVendor());

            for (Telecom t : vendorTelecomList) {
                if (t.getTelecom_id().equals(telecomId)) {
                    return v;
                }
            }
        }
        throw new NotFoundException("Vendor not found!");
    }

    // FOR DEAL SUPPORT TICKET

    public List<Deal> retrieveAllDealsByVendor(Vendor vendor) throws NotFoundException {

        if (!vendor.getDeals_list().isEmpty()) {
            return vendor.getDeals_list();
        } else {
            throw new NotFoundException("Deals not found!");
        }
    }

    public VendorStaff getVendorByDeal(Long dealId) throws BadRequestException, NotFoundException {

        Optional<Deal> dealOptional = dealRepository.findById(dealId);
        if (dealOptional.isEmpty()) {
            throw new BadRequestException("Deal does not exist!");
        }

        List<VendorStaff> vendorStaffList = retrieveAllVendorStaff();

        for (VendorStaff v : vendorStaffList) {
            List<Deal> vendorDealList = retrieveAllDealsByVendor(v.getVendor());

            for (Deal d : vendorDealList) {
                if (d.getDeal_id().equals(dealId)) {
                    return v;
                }
            }
        }
        throw new NotFoundException("Vendor not found!");
    }


    public SupportTicket createSupportTicketForBooking(Long userId, Long bookingId, SupportTicket supportTicketToCreate) throws BadRequestException, NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        supportTicketToCreate.setIs_resolved(false);
        supportTicketToCreate.setReply_list(new ArrayList<>());

        VendorStaff vendorStaff = null;

        Booking booking = bookingService.getBookingByBookingId(bookingId);
        booking.setLocal_user(null);
        booking.setTourist_user(null);
        supportTicketToCreate.setBooking(booking);

        if (booking.getAttraction() != null) {
            Attraction attraction = booking.getAttraction();
            vendorStaff = getVendorByAttraction(attraction.getAttraction_id());
        } else if (booking.getRoom() != null) {
            Room room = booking.getRoom();
            Accommodation accommodation = accommodationService.retrieveAccommodationByRoom(room.getRoom_id());
            vendorStaff = getVendorByAccommodation(accommodation.getAccommodation_id());
        } else if (booking.getTelecom() != null) {
            Telecom telecom = booking.getTelecom();
            vendorStaff = getVendorByTelecom(telecom.getTelecom_id());
        } else if (booking.getDeal() != null) {
            Deal deal = booking.getDeal();
            vendorStaff = getVendorByDeal(deal.getDeal_id());
        }
        // tour if adding

        SupportTicket supportTicket = supportTicketRepository.save(supportTicketToCreate);
        vendorStaff.getIncoming_support_ticket_list().add(supportTicket);
        vendorStaffRepository.save(vendorStaff);

        User user = userOptional.get();
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
        supportTicket.setTicket_category(supportTicketToUpdate.getTicket_category());
        supportTicket.setTicket_type(supportTicketToUpdate.getTicket_type());

        supportTicketRepository.save(supportTicket);

        return supportTicket;
    }

    public void deleteSupportTicket(Long supportTicketId) throws NotFoundException, BadRequestException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support Ticket does not exist!");
        }

        SupportTicket supportTicket = supportTicketOptional.get();
        List<SupportTicket> supportTicketList = getAllSupportTickets();

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

            supportTicketRepository.deleteById(supportTicketId);

        }

    }


    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

}