package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.repositories.InternalStaffRepository;

import org.hibernate.Internal;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InternalStaffService {
    @Autowired
    InternalStaffRepository internalStaffRepository;
    @Autowired
    JavaMailSender javaMailSender;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public InternalStaff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.getInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, internalStaff.getPassword())
                && !internalStaff.getIs_blocked()) {
            internalStaff.setComment_list(null);
            internalStaff.setPost_list(null);
            internalStaff.setBadge_list(null);
            internalStaff.setSupport_ticket_list(null);
            return internalStaff;
        } else if (internalStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

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

    public List<InternalStaff> retrieveAllStaff() {
        List<InternalStaff> internalStaffList = internalStaffRepository.findAll();

        for (InternalStaff i : internalStaffList) {
            i.setPassword(null);
        }

        return internalStaffList;
    }

    public InternalStaff getStaffProfile(Long staffId) throws IllegalArgumentException, AdminNotFoundException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffId);

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();
                internalStaff.setPassword(null);
                return internalStaff;

            } else {
                 throw new AdminNotFoundException("Admin staff not found!");
            }

        } catch(Exception ex) {
            throw new AdminNotFoundException(ex.getMessage());
        }
    }

    public InternalStaff editStaffProfile(InternalStaff staffToEdit) throws EditAdminException {
        try {

            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffToEdit.getUser_id());

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();

                Long existingId = internalStaffRepository.getAdminByEmail(staffToEdit.getEmail());
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

    public void editInternalStaffPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(id);

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, internalStaff.getPassword())) {
                    internalStaff.setPassword(encoder.encode(newPassword));
                    internalStaffRepository.save(internalStaff);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("User not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        String passwordResetToken = UUID.randomUUID().toString();
        InternalStaff internalStaff = internalStaffRepository.getInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        internalStaff.setPassword_reset_token(passwordResetToken);
        internalStaff.setToken_date(LocalDateTime.now());
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
        System.out.println(token);
        InternalStaff internalStaff = internalStaffRepository.getInternalStaffByToken(token);

        if (internalStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(internalStaff.getToken_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        internalStaff.setPassword(encoder.encode(password));
        internalStaff.setPassword_reset_token(null);
        internalStaff.setToken_date(null);
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

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }
}
