package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;

import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VendorStaffService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public VendorStaff vendorStaffLogin(String email, String password) throws NotFoundException, BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

        if (vendorStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, vendorStaff.getPassword())
                && !vendorStaff.getIs_blocked()
                && vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.APPROVED) {
            vendorStaff.getVendor().setWithdrawal_list(null);
            vendorStaff.getVendor().setVendor_staff_list(null);
            vendorStaff.getVendor().setComment_list(null);
            vendorStaff.getVendor().setPost_list(null);
            vendorStaff.getVendor().setAttraction_list(null);
            vendorStaff.getVendor().setAccommodation_list(null);
            vendorStaff.getVendor().setRestaurant_list(null);
            vendorStaff.getVendor().setTelecom_list(null);
            vendorStaff.getVendor().setDeals_list(null);
            return vendorStaff;
        } else if (vendorStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator!");
        }
        else if (vendorStaff.getVendor().getApplication_status() != ApplicationStatusEnum.APPROVED) {
            throw new BadRequestException("Your application is still pending review!");
        }
        else {
            throw new BadRequestException("Incorrect password!");
        }
    }

    public void updateVendorStaff(VendorStaff vendorStaffToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById((vendorStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));

        if (vendorStaff.getEmail().equals(vendorStaffToUpdate.getEmail())) {
            if (vendorStaffToUpdate.getName() != null && !vendorStaffToUpdate.getName().isEmpty()) {
                vendorStaff.setName(vendorStaffToUpdate.getName());
            }
        }

        vendorStaffRepository.save(vendorStaff);
    }

    public Long createVendorStaff(VendorStaff vendorStaffToCreate) throws BadRequestException  {
        Long existingId = userRepository.getUserIdByEmail(vendorStaffToCreate.getEmail());

        if (existingId != null) {
            throw new BadRequestException("Email address in used, please enter another email!");
        }

        Vendor vendorToCreate = vendorStaffToCreate.getVendor();
        vendorRepository.save(vendorToCreate);

        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        vendorStaffRepository.save(vendorStaffToCreate);

        try {
            String subject = "[WithinSG] Account Application Processing";
            String content = "<p>Dear " + vendorStaffToCreate.getName() + ",</p>" +
                    "<p>Thank you for registering for a vendor account with WithinSG. " +
                    "We are glad that you have chosen us as your service provider!</p>" +
                    "<p>We have received your application and it is in the midst of processing.</p>" +
                    "<p>An email will be sent to you once your account has been activated.</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(vendorStaffToCreate.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return vendorStaffToCreate.getUser_id();
    }

    public List<VendorStaff> getAllVendorStaff() {
        return vendorStaffRepository.findAll();
    }

    public List<VendorStaff> getAllAssociatedVendorStaff(Long vendorId) {
        List<VendorStaff> vendorStaffs = vendorStaffRepository.getAllAssociatedVendorStaff(vendorId);

        for (VendorStaff vs : vendorStaffs) {
            vs.getVendor().setVendor_staff_list(null);
        }

        return vendorStaffs;
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        String passwordResetToken = UUID.randomUUID().toString();
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

        if (vendorStaff == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        vendorStaff.setPassword_reset_token(passwordResetToken);
        vendorStaff.setToken_date(LocalDateTime.now());
        vendorStaffRepository.save(vendorStaff);
        String passwordResetLink = "http://localhost:3000/passwordreset?token=" + vendorStaff.getPassword_reset_token();
        try {
            String subject = "[WithinSG] Password Reset Instructions";
            String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>You can reset your password by clicking on the button below: </p>" +
                    "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                    "Reset Password</button></a>" +
                    "<p>Note that the link will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(vendorStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String token, String password) throws BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByToken(token);

        if (vendorStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(vendorStaff.getToken_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        vendorStaff.setPassword(encoder.encode(password));
        vendorStaff.setPassword_reset_token(null);
        vendorStaff.setToken_date(null);
        vendorStaffRepository.save(vendorStaff);

        try {
            String subject = "[WithinSG] Password Reset Successfully";
            String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(vendorStaff.getEmail(), subject, content);
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

    public VendorStaff getVendorStaffProfile(Long vendorStaffId) throws IllegalArgumentException, VendorStaffNotFoundException {
        try {
            Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(vendorStaffId);

            if (vendorStaffOptional.isPresent()) {
                VendorStaff vendorStaff = vendorStaffOptional.get();
                vendorStaff.setPassword(null);
                vendorStaff.setVendor(null);
                return vendorStaff;

            } else {
                throw new VendorStaffNotFoundException("Vendor staff not found!");
            }

        } catch(Exception ex) {
            throw new VendorStaffNotFoundException(ex.getMessage());
        }
    }

    public VendorStaff editVendorStaffProfile(VendorStaff vendorStaffToEdit) throws EditVendorStaffException {
        try {

            Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(vendorStaffToEdit.getUser_id());

            if (vendorStaffOptional.isPresent()) {
                VendorStaff vendorStaff = vendorStaffOptional.get();

                if (!vendorStaff.getEmail().equals(vendorStaffToEdit.getEmail())) { // user wants to change email
                    Long existingId = vendorStaffRepository.getVendorStaffByEmail(vendorStaffToEdit.getEmail());
                    if (existingId != null) { // but there is an existing email
                        throw new EditVendorStaffException("Email currently in use. Please use a different email!");
                    }
                }

                vendorStaff.setEmail(vendorStaffToEdit.getEmail());
                vendorStaff.setName(vendorStaffToEdit.getName());
                vendorStaff.setPosition(vendorStaffToEdit.getPosition());
                vendorStaffRepository.save(vendorStaff);
                vendorStaff.setVendor(null);
                vendorStaff.setPassword(null);
                return vendorStaff;

            } else {
                throw new EditVendorStaffException("Admin staff not found!");
            }
        } catch (Exception ex) {
            throw new EditVendorStaffException(ex.getMessage());
        }
    }

    public void editVendorStaffPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(id);

            if (vendorStaffOptional.isPresent()) {
                VendorStaff vendorStaff = vendorStaffOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, vendorStaff.getPassword())) {
                    vendorStaff.setPassword(encoder.encode(newPassword));
                    vendorStaffRepository.save(vendorStaff);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditVendorStaffException("Vendor staff not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}
