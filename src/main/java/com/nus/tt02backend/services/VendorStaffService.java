package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.User;
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

        // Local
        if (vendorStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, vendorStaff.getPassword())
                && !vendorStaff.getIs_blocked()
                && vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.APPROVED
                && vendorStaff.getEmail_verified()) {
            return vendorStaff;
        } else if (!vendorStaff.getEmail_verified()) {
            String emailVerificationLink = "http://localhost:3000/verifyemail?token=" + vendorStaff.getEmail_verification_token();
            try {
                String subject = "[WithinSG] Email Verification Required";
                String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                        "<p>You have not verified your email address.</p>" +
                        "<p>Please verify your email address by clicking on the button below.</p>" +
                        "<a href=\"" + emailVerificationLink +"\" target=\"_blank\">" +
                        "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                        "Verify Email</button></a>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(vendorStaff.getEmail(), subject, content);
            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
            }

            throw new BadRequestException("Your email has not been verified, a new email verification email has been sent to you");
        } else if (vendorStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator");
        }
        else if (vendorStaff.getVendor().getApplication_status() != ApplicationStatusEnum.APPROVED) {
            throw new BadRequestException("Your application is still pending review");
        }
        else {
            throw new BadRequestException("Incorrect password");
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

        Long existingId = vendorStaffRepository.getVendorStaffIdByEmail(vendorStaffToCreate.getEmail());
        if (existingId != null && existingId != vendorStaffToCreate.getUser_id()) { // but there is an existing email
            throw new BadRequestException("Email currently in use. Please use a different email!");
        }

        Vendor vendorToCreate = vendorStaffToCreate.getVendor();
        vendorRepository.save(vendorToCreate);

        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        String emailVerificationToken = UUID.randomUUID().toString();
        vendorStaffToCreate.setEmail_verification_token(emailVerificationToken);
        vendorStaffRepository.save(vendorStaffToCreate);

        String emailVerificationLink = "http://localhost:3000/verifyemail?token=" + vendorStaffToCreate.getEmail_verification_token();
        try {
            String subject = "[WithinSG] Account Application Processing";
            String content = "<p>Dear " + vendorStaffToCreate.getName() + ",</p>" +
                    "<p>Thank you for registering for a vendor account with WithinSG. " +
                    "We are glad that you have chosen us as your service provider!</p>" +
                    "<p>We have received your application and it is in the midst of processing. " +
                    "Please verify your email address by clicking on the button below.</p>" +
                    "<a href=\"" + emailVerificationLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                    "Verify Email</button></a>" +
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
        vendorStaff.setPassword_token_date(LocalDateTime.now());
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
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByPasswordToken(token);

        if (vendorStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(vendorStaff.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        vendorStaff.setPassword(encoder.encode(password));
        vendorStaff.setPassword_reset_token(null);
        vendorStaff.setPassword_token_date(null);
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

    public String verifyEmail(String token) throws BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmailToken(token);

        if (vendorStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (!vendorStaff.getEmail_verified()) {
            vendorStaff.setEmail_verified(true);
            vendorStaffRepository.save(vendorStaff);

            try {
                String subject = "[WithinSG] Email Verified Successfully";
                String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                        "<p>Your email has been verified successfully." +
                        "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(vendorStaff.getEmail(), subject, content);
            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the successful email verification email");
            }
        }

        return "Your email has been verified successfully";
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
                System.out.println("tanweekek:" + vendorStaff.getUser_id());

                Long existingId = vendorStaffRepository.getVendorStaffIdByEmail(vendorStaffToEdit.getEmail());
                if (existingId != null && existingId != vendorStaffToEdit.getUser_id()) { // but there is an existing email
                    throw new EditVendorStaffException("Email currently in use. Please use a different email!");
                }

                vendorStaff.setEmail(vendorStaffToEdit.getEmail());
                vendorStaff.setName(vendorStaffToEdit.getName());
                vendorStaff.setPosition(vendorStaffToEdit.getPosition());
                // fetch vendor id, edit attributes and save
                vendorStaffRepository.save(vendorStaff);
                vendorStaff.setPassword(null);
                vendorStaff.getVendor().setVendor_staff_list(null);
                return vendorStaff;

            } else {
                throw new EditVendorStaffException("Vendor staff not found!");
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

    public void toggleBlock(Long vendorStaffId) throws NotFoundException, ToggleBlockException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(vendorStaffId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            if (vendorStaff.getIs_master_account() && !vendorStaff.getIs_blocked()) { // master vendor staff
                throw new ToggleBlockException("Vendor Staff access rights prevents him from being blocked!");
            }
            vendorStaff.setIs_blocked(!vendorStaff.getIs_blocked());
            vendorStaffRepository.save(vendorStaff);

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }
    }
}
