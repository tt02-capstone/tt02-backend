package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VendorService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public VendorStaff vendorLogin(String email, String password) throws NotFoundException, BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

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
        else if (!encoder.matches(password, vendorStaff.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }
        else {
            throw new BadRequestException("Your application is still pending review");
        }
    }

    public void updateVendor(VendorStaff vendorStaffToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById((vendorStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));

        if (vendorStaff.getEmail().equals(vendorStaffToUpdate.getEmail())) {
            if (vendorStaffToUpdate.getName() != null && !vendorStaffToUpdate.getName().isEmpty()) {
                vendorStaff.setName(vendorStaffToUpdate.getName());
            }
        }

        vendorStaffRepository.save(vendorStaff);
    }

    public Long createVendor(VendorStaff vendorStaffToCreate) throws BadRequestException  {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(vendorStaffToCreate.getEmail());

        if (vendorStaff != null) {
            throw new BadRequestException("The email address has been used, please enter another email");
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

    public List<VendorStaff> retrieveAllVendors() {
        return vendorStaffRepository.findAll();
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
}
