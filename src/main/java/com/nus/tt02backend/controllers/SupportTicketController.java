package com.nus.tt02backend.controllers;

import com.nus.tt02backend.models.*;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.services.SupportTicketService;
import com.nus.tt02backend.services.ReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/supportTicket")
public class SupportTicketController {

    @Autowired
    SupportTicketService supportTicketService;

    @Autowired
    ReplyService replyService;

    @GetMapping("/getAllSupportTickets")
    public ResponseEntity<List<SupportTicket>> getAllSupportTickets() throws NotFoundException, BadRequestException {
        List<SupportTicket> supportTicketList = supportTicketService.getAllSupportTickets();
        return ResponseEntity.ok(supportTicketList);
    }

    @GetMapping("/getAllSupportTicketsByUser/{userId}")
    public ResponseEntity<List<SupportTicket>> getAllSupportTicketsByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<SupportTicket> supportTicketList = supportTicketService.getAllSupportTicketsByUser(userId);
        return ResponseEntity.ok(supportTicketList);
    }

    @GetMapping("/getAllSupportTicketsByAdmin/{userId}")
    public ResponseEntity<List<SupportTicket>> getAllSupportTicketsByAdmin(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<SupportTicket> supportTicketList = supportTicketService.getAllSupportTicketsByAdmin(userId);
        return ResponseEntity.ok(supportTicketList);
    }

    @GetMapping("/getAllOutgoingSupportTicketsByVendorStaff/{userId}")
    public ResponseEntity<List<SupportTicket>> getAllOutgoingSupportTicketsByVendorStaff(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<SupportTicket> supportTicketList = supportTicketService.getAllOutgoingSupportTicketsByVendorStaff(userId);
        return ResponseEntity.ok(supportTicketList);
    }

    @GetMapping("/getAllIncomingSupportTicketsByVendorStaff/{userId}")
    public ResponseEntity<List<SupportTicket>> getAllIncomingSupportTicketsByVendorStaff(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<SupportTicket> supportTicketList = supportTicketService.getAllIncomingSupportTicketsByVendorStaff(userId);
        return ResponseEntity.ok(supportTicketList);
    }

    @GetMapping("/getSupportTicket/{supportTicketId}")
    public ResponseEntity<SupportTicket> getSupportTicket(@PathVariable Long supportTicketId) throws NotFoundException, BadRequestException {
        SupportTicket supportTicket = supportTicketService.getSupportTicket(supportTicketId);
        return ResponseEntity.ok(supportTicket);
    }

    @PostMapping("/createSupportTicketToAdmin/{userId}")
    public ResponseEntity<SupportTicket> createSupportTicketToAdmin(@PathVariable Long userId, @RequestBody SupportTicket supportTicketToCreate) throws BadRequestException {
        SupportTicket supportTicket = supportTicketService.createSupportTicketToAdmin(userId, supportTicketToCreate);
        return ResponseEntity.ok(supportTicket);
    }

    @PostMapping("/createSupportTicketToVendor/{userId}/{activityId}")
    public ResponseEntity<SupportTicket> createSupportTicketToVendor(@PathVariable Long userId, @PathVariable Long activityId, @RequestBody SupportTicket supportTicketToCreate) throws BadRequestException, NotFoundException {
        SupportTicket supportTicket = supportTicketService.createSupportTicketToVendor(userId, activityId, supportTicketToCreate);
        return ResponseEntity.ok(supportTicket);
    }

    @GetMapping("/getAllBookingsByUser/{userId}")
    public ResponseEntity<List<Booking>> getAllBookingsByUser(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        List<Booking> bookingList = supportTicketService.getAllBookingsByUser(userId);
        return ResponseEntity.ok(bookingList);
    }

    @GetMapping("/getBookingByBookingId/{bookingId}")
    public ResponseEntity<Booking> getBookingByBookingId(@PathVariable Long bookingId) throws NotFoundException {
        Booking booking = supportTicketService.getBookingByBookingId(bookingId);
        return ResponseEntity.ok(booking);
    }

    @PostMapping("/createSupportTicketForBooking/{userId}/{bookingId}")
    public ResponseEntity<SupportTicket> createSupportTicketForBooking(@PathVariable Long userId, @PathVariable Long bookingId, @RequestBody SupportTicket supportTicketToCreate) throws BadRequestException, NotFoundException {
        SupportTicket supportTicket = supportTicketService.createSupportTicketForBooking(userId, bookingId, supportTicketToCreate);
        return ResponseEntity.ok(supportTicket);
    }

    @PutMapping("/updateSupportTicketStatus/{supportTicketId}")
    public ResponseEntity<SupportTicket> updateSupportTicketStatus(@PathVariable Long supportTicketId) throws IllegalArgumentException, NotFoundException {
        SupportTicket supportTicket = supportTicketService.updateSupportTicketStatus(supportTicketId);
        return ResponseEntity.ok(supportTicket);
    }

    @PutMapping("/updateSupportTicket/{supportTicketId}")
    public ResponseEntity<SupportTicket> updateSupportTicket(@PathVariable Long supportTicketId, @RequestBody SupportTicket supportTicketToUpdate) throws IllegalArgumentException, NotFoundException, BadRequestException {
        SupportTicket supportTicket = supportTicketService.updateSupportTicket(supportTicketId, supportTicketToUpdate);
        return ResponseEntity.ok(supportTicket);
    }

    @DeleteMapping ("/deleteSupportTicket/{supportTicketId}")
    public ResponseEntity<Void> deleteSupportTicket(@PathVariable Long supportTicketId) throws NotFoundException, BadRequestException {
        supportTicketService.deleteSupportTicket(supportTicketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getAllRepliesBySupportTicket/{supportTicketId}")
    public ResponseEntity<List<Reply>> getAllRepliesBySupportTicket(@PathVariable Long supportTicketId) throws NotFoundException, BadRequestException {
        List<Reply> replyList = replyService.getAllRepliesBySupportTicket(supportTicketId);
        return ResponseEntity.ok(replyList);
    }

    @GetMapping("/getReplyById/{replyId}")
    public ResponseEntity<Reply> getReplyById(@PathVariable Long replyId) throws NotFoundException, BadRequestException {
        Reply reply = replyService.getReplyById(replyId);
        return ResponseEntity.ok(reply);
    }

    @PostMapping("/createReply/{userId}/{supportTicketId}")
    public ResponseEntity<Reply> createReply(@PathVariable Long userId, @PathVariable Long supportTicketId, @RequestBody Reply replyToCreate) throws BadRequestException, NotFoundException {
        Reply reply = replyService.createReply(userId, supportTicketId, replyToCreate);
        return ResponseEntity.ok(reply);
    }

    @PutMapping("/updateReply/{replyId}")
    public ResponseEntity<Reply> updateReply(@PathVariable Long replyId, @RequestBody Reply replyToUpdate) throws NotFoundException, BadRequestException {
        Reply reply = replyService.updateReply(replyId, replyToUpdate);
        return ResponseEntity.ok(reply);
    }

    @DeleteMapping ("/deleteReply/{supportTicketId}/{replyId}")
    public ResponseEntity<List<Reply>> deleteReply(@PathVariable Long supportTicketId ,@PathVariable Long replyId) throws NotFoundException, BadRequestException {
        List<Reply> replyList =  replyService.deleteReply(supportTicketId,replyId);
        return ResponseEntity.ok(replyList);
    }

    @GetMapping("/getUserAvatarImage/{userId}")
    public ResponseEntity<String> getUserAvatarImage(@PathVariable Long userId) throws NotFoundException, BadRequestException {
        String image = supportTicketService.getUserAvatarImage(userId);
        return ResponseEntity.ok(image);
    }
}
