package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.*;

@Service
public class InternalStaffService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    InternalStaffRepository internalStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    JavaMailSender javaMailSender;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public void updateStaff(InternalStaff internalStaffToUpdate) throws NotFoundException {
        InternalStaff internalStaff = internalStaffRepository.findById((internalStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("InternalStaff not found"));

        if (internalStaff.getEmail().equals(internalStaffToUpdate.getEmail())) {
            if (internalStaffToUpdate.getUser_id() != null && !internalStaffToUpdate.getName().isEmpty()) {
                internalStaff.setName(internalStaffToUpdate.getName());
            }
        }

        internalStaffRepository.save(internalStaff);
    }

    public Long createStaff(InternalStaff internalStaffToCreate) throws BadRequestException {

        Long existingId = internalStaffRepository.getAdminByEmail(internalStaffToCreate.getEmail());
        if (existingId != null && existingId != internalStaffToCreate.getUser_id()) { // but there is an existing email
            throw new BadRequestException("Email currently in use. Please use a different email!");
        }

        Long latestStaffNum = internalStaffRepository.getLatestStaffNum();
        if (latestStaffNum == null || latestStaffNum < 1L) {
            latestStaffNum = 1L;
        } else {
            latestStaffNum++;
        }

        internalStaffToCreate.setStaff_num(latestStaffNum);
        internalStaffToCreate.setPassword(encoder.encode(internalStaffToCreate.getPassword()));
        internalStaffRepository.save(internalStaffToCreate);

        try {
            String subject = "[WithinSG] Staff Account Created";
            String content = "<p>Dear " + internalStaffToCreate.getName() + ",</p>" +
                    "<p>A staff account has been created for you.</p>" +
                    "<p>Please sign in using your staff number as the password. " +
                    "You will be prompted to change your password upon signing in for the first time.</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaffToCreate.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return internalStaffToCreate.getUser_id();
    }

    public List<InternalStaff> retrieveAllAdmin() {
        List<InternalStaff> internalStaffList = internalStaffRepository.findAll();

        for (InternalStaff i : internalStaffList) {
            i.setPassword(null);
        }

        return internalStaffList;
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        String passwordResetToken = UUID.randomUUID().toString();
        InternalStaff internalStaff = internalStaffRepository.getInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        internalStaff.setPassword_reset_token(passwordResetToken);
        internalStaff.setPassword_token_date(LocalDateTime.now());
        internalStaffRepository.save(internalStaff);
        String passwordResetLink = "http://localhost:3000/passwordreset?token=" + internalStaff.getPassword_reset_token();
        try {
            String subject = "[WithinSG] Password Reset Instructions";
            String content = "<p>Dear " + internalStaff.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>You can reset your password by clicking on the button below: </p>" +
                    "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                    "Reset Password</button></a>" +
                    "<p>Note that the link will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String token, String password) throws BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByToken(token);

        if (internalStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(internalStaff.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        internalStaff.setPassword(encoder.encode(password));
        internalStaff.setPassword_reset_token(null);
        internalStaff.setPassword_token_date(null);
        internalStaffRepository.save(internalStaff);

        try {
            String subject = "[WithinSG] Password Reset Successfully";
            String content = "<p>Dear " + internalStaff.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "Your password has been changed successfully";
    }

    public List<Vendor> getPendingApplications() {
        List<Vendor> vendors = vendorRepository.retrievePendingVendorApplications(ApplicationStatusEnum.PENDING);
        for (Vendor vendor : vendors) {
            vendor.setVendor_staff_list(null);
        }

        return vendors;
    }

    public String updateApplicationStatus(Long vendorId, ApplicationStatusEnum applicationStatus) throws NotFoundException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new NotFoundException("Invalid Vendor ID");
        }

        Vendor vendor = vendorOptional.get();
        vendor.setApplication_status(applicationStatus);
        vendorRepository.save(vendor);

        return "Application status updated successfully";
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public InternalStaff editProfile(InternalStaff staffToEdit) throws EditAdminException {
        try {

            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffToEdit.getUser_id());

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();

                Long existingId = userRepository.retrieveIdByUserEmail(staffToEdit.getEmail());
                if (existingId != null && existingId != staffToEdit.getUser_id()) { // but there is an existing email
                    throw new EditAdminException("Email currently in use. Please use a different email!");
                }

                internalStaff.setEmail(staffToEdit.getEmail());
                internalStaff.setName(staffToEdit.getName());
                internalStaffRepository.save(internalStaff);
                internalStaff.setPassword(null);
                return internalStaff;

            } else {
                throw new EditAdminException("Admin staff not found!");
            }
        } catch (Exception ex) {
            throw new EditAdminException(ex.getMessage());
        }
    }
}
