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

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }
}
