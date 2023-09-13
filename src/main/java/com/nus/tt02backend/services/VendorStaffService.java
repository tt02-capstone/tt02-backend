package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.VendorStaff;
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
import java.util.UUID;

@Service
public class VendorStaffService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public String passwordResetStageOne(String email) throws BadRequestException {
        String passwordResetToken = UUID.randomUUID().toString();
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

        if (vendorStaff == null) {
            throw new BadRequestException("There is no vendor staff account associated with this email address");
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
