package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingStatusEnum;
import com.nus.tt02backend.models.enums.SupportTicketCategoryEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ReplyService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    SupportTicketRepository supportTicketRepository;

    public List<Reply> getAllRepliesBySupportTicket(Long supportTicketId) throws NotFoundException, BadRequestException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support ticket does not exist!");
        }
        SupportTicket supportTicket = supportTicketOptional.get();

        List<Reply> replyList = supportTicket.getReply_list();

        for (Reply r : replyList) {
            if (r.getVendor_staff_user() != null) {
                r.getVendor_staff_user().setVendor(null);
                r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
            } else if (r.getInternal_staff_user() != null) {
                r.getInternal_staff_user().setSupport_ticket_list(null);
            } else if (r.getTourist_user() != null) {
                r.getTourist_user().setSupport_ticket_list(null);
            } else if (r.getLocal_user() != null) {
                r.getLocal_user().setSupport_ticket_list(null);
            }
        }

        return replyList;
    }

    public Reply getReplyById(Long replyId) throws NotFoundException, BadRequestException {

        Optional<Reply> replyOptional = replyRepository.findById(replyId);
        if (replyOptional.isEmpty()) {
            throw new BadRequestException("Reply does not exist!");
        }

        Reply reply = replyOptional.get();

        if (reply.getVendor_staff_user() != null) {
            reply.getVendor_staff_user().setVendor(null);
            reply.getVendor_staff_user().setIncoming_support_ticket_list(null);
            reply.getVendor_staff_user().setOutgoing_support_ticket_list(null);
        } else if (reply.getInternal_staff_user() != null) {
            reply.getInternal_staff_user().setSupport_ticket_list(null);
        } else if (reply.getTourist_user() != null) {
            reply.getTourist_user().setSupport_ticket_list(null);
        } else if (reply.getLocal_user() != null) {
            reply.getLocal_user().setSupport_ticket_list(null);
        }

        return replyOptional.get();
    }

    public Reply createReply(Long userId, Long supportTicketId, Reply replyToCreate) throws NotFoundException, BadRequestException {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support ticket does not exist!");
        }

        replyToCreate.setCreated_time(LocalDateTime.now());
        replyToCreate.setUpdated_time(LocalDateTime.now());
        Reply reply = replyRepository.save(replyToCreate);

        SupportTicket supportTicket = supportTicketOptional.get();
        List<Reply> replyList = supportTicket.getReply_list();
        replyList.add(reply);
        supportTicket.setReply_list(replyList);
        supportTicketRepository.save(supportTicket);

        User user = userOptional.get();
        if (user.getUser_type().equals(UserTypeEnum.TOURIST)) {
            Tourist tourist = (Tourist) user;
            reply.setTourist_user(tourist);
            replyRepository.save(reply);

            reply.getTourist_user().setSupport_ticket_list(null);

        } else if (user.getUser_type().equals(UserTypeEnum.LOCAL)) {
            Local local = (Local) user;
            reply.setLocal_user(local);
            replyRepository.save(reply);

            reply.getLocal_user().setSupport_ticket_list(null);

        } else if (user.getUser_type().equals(UserTypeEnum.VENDOR_STAFF)) {
            VendorStaff vendorStaff = (VendorStaff) user;
            reply.setVendor_staff_user(vendorStaff);
            replyRepository.save(reply);

            reply.getVendor_staff_user().getVendor().setVendor_staff_list(null);
            reply.getVendor_staff_user().getVendor().setAttraction_list(null);
            reply.getVendor_staff_user().getVendor().setAccommodation_list(null);
            reply.getVendor_staff_user().getVendor().setTelecom_list(null);
            reply.getVendor_staff_user().getVendor().setRestaurant_list(null);
            reply.getVendor_staff_user().getVendor().setDeals_list(null);
            reply.getVendor_staff_user().setIncoming_support_ticket_list(null);
            reply.getVendor_staff_user().setOutgoing_support_ticket_list(null);

        } else if (user.getUser_type().equals(UserTypeEnum.INTERNAL_STAFF)) {
            InternalStaff internalStaff = (InternalStaff) user;
            reply.setInternal_staff_user(internalStaff);
            replyRepository.save(reply);

            reply.getInternal_staff_user().setSupport_ticket_list(null);
        }

        return reply;
    }

    public Reply updateReply(Long replyId, Reply replyToUpdate) throws NotFoundException, BadRequestException {

        Optional<Reply> replyOptional = replyRepository.findById(replyId);
        if (replyOptional.isEmpty()) {
            throw new BadRequestException("Reply does not exist!");
        }

        Reply reply = replyOptional.get();
        reply.setMessage(replyToUpdate.getMessage());
        reply.setUpdated_time(LocalDateTime.now());
        replyRepository.save(reply);

        if (reply.getVendor_staff_user() != null) {
            reply.getVendor_staff_user().setVendor(null);
            reply.getVendor_staff_user().setIncoming_support_ticket_list(null);
            reply.getVendor_staff_user().setOutgoing_support_ticket_list(null);
        } else if (reply.getInternal_staff_user() != null) {
            reply.getInternal_staff_user().setSupport_ticket_list(null);
        } else if (reply.getTourist_user() != null) {
            reply.getTourist_user().setSupport_ticket_list(null);
        } else if (reply.getLocal_user() != null) {
            reply.getLocal_user().setSupport_ticket_list(null);
        }

        return reply;
    }

    public List<Reply> deleteReply(Long supportTicketId, Long replyId) throws NotFoundException, BadRequestException {

        Optional<SupportTicket> supportTicketOptional = supportTicketRepository.findById(supportTicketId);
        if (supportTicketOptional.isEmpty()) {
            throw new BadRequestException("Support Ticket does not exist!");
        }

        Optional<Reply> replyOptional = replyRepository.findById(replyId);
        if (replyOptional.isEmpty()) {
            throw new BadRequestException("Reply does not exist!");
        }

        SupportTicket supportTicket = supportTicketOptional.get();
        List<Reply> currentReplyList = supportTicket.getReply_list();

        for (Reply r : currentReplyList) {
            if(r.getReply_id().equals(replyId)) {

                currentReplyList.remove(r);
                supportTicket.setReply_list(currentReplyList);
                supportTicketRepository.save(supportTicket);
                replyRepository.deleteById(replyId);
                break;
            } else {
                if (r.getVendor_staff_user() != null) {
                    r.getVendor_staff_user().setVendor(null);
                    r.getVendor_staff_user().setIncoming_support_ticket_list(null);
                    r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
                } else if (r.getLocal_user() != null) {
                    r.getLocal_user().setSupport_ticket_list(null);
                } else if (r.getTourist_user() != null) {
                    r.getTourist_user().setSupport_ticket_list(null);
                } else if (r.getInternal_staff_user() != null) {
                    r.getInternal_staff_user().setSupport_ticket_list(null);
                }
            }
        }

        return currentReplyList;
    }
}
