package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.repositories.TouristRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TouristService {
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Tourist touristLogin(String email, String password) throws NotFoundException, BadRequestException {
        Tourist checkTourist = touristRepository.retrieveTouristByEmail(email);

        if (checkTourist == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkTourist.getPassword())
                && !checkTourist.getIs_blocked()) {
            checkTourist.setComment_list(new ArrayList<>());
            checkTourist.setPost_list(null);
            checkTourist.setBadge_list(null);
            checkTourist.setSupport_ticket_list(null);
            checkTourist.setAttraction_list(null);
            checkTourist.setPost_list(null);
            checkTourist.setAccommodation_list(null);
            checkTourist.setCard_list(null);
            checkTourist.setCart_list(null);
            checkTourist.setDeal_list(null);
            checkTourist.setRestaurant_list(null);
            checkTourist.setTelecom_list(null);
            checkTourist.setTour_type_list(null);
            checkTourist.setItinerary(null);
            return checkTourist;

        } else if (checkTourist.getIs_blocked()) {
            throw new BadRequestException("Your account is disabled, please contact our help desk");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public void updateTourist(Tourist touristToUpdate) throws NotFoundException {
        Tourist tourist = touristRepository.findById((touristToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("Rourist not found"));

        if (tourist.getEmail().equals(touristToUpdate.getEmail())) {
            if (touristToUpdate.getName() != null && !touristToUpdate.getName().isEmpty()) {
                tourist.setName(touristToUpdate.getName());
            }
        }

        touristRepository.save(tourist);
    }

    public Long createTourist(Tourist touristToCreate) throws BadRequestException {
        Tourist checkTourist = touristRepository.retrieveTouristByEmail(touristToCreate.getEmail());

        if (checkTourist != null) {
            throw new BadRequestException("The email address has been used to register another account, please enter another email");
        }

        touristToCreate.setPassword(encoder.encode(touristToCreate.getPassword()));
        touristRepository.save(touristToCreate);

        try {
            String subject = "[WithinSG] User Account Application Processing";
            String content = "<p>Dear " + touristToCreate.getName() + ",</p>" +
                    "<p>Thank you for registering for an account with WithinSG. " +
                    "We are glad that you have chosen us to help you explore Singapore!</p>" +
                    "<p>We have received your application and it is in the midst of processing.</p>" +
                    "<p>An email will be sent to you once your account has been activated.</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(touristToCreate.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return touristToCreate.getUser_id();
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);;
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        UUID uuid = UUID.randomUUID();
        long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
        String passwordResetOTP =  String.format("%04d", otpValue);

        Tourist tourist = touristRepository.retrieveTouristByEmail(email);

        if (tourist == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        tourist.setPassword_reset_token(passwordResetOTP);
        tourist.setToken_date(LocalDateTime.now());
        touristRepository.save(tourist);

        try {
            String subject = "[WithinSG] Your Password Reset Instructions";
            String content = "<p>Dear " + tourist.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>Please enter your vertification code into the WithinSG application: </p>" +
                    "<a href=\"" + passwordResetOTP +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" + passwordResetOTP + "</button></a>" +
                    "<p>Note that the code will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(tourist.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String token) throws BadRequestException {
        System.out.println(token);
        Tourist tourist = touristRepository.retrieveTouristByToken(token);

        if (tourist == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(tourist.getToken_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        return "The code is verified correctly";
    }

    public String passwordResetStageThree(String token, String password) throws BadRequestException {
        System.out.println(token);
        Tourist tourist = touristRepository.retrieveTouristByToken(token);

        if (tourist == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(tourist.getToken_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        tourist.setPassword(encoder.encode(password));
        tourist.setPassword_reset_token(null);
        tourist.setToken_date(null);
        touristRepository.save(tourist);

        try {
            String subject = "[WithinSG] Your Password was reset Successfully";
            String content = "<p>Dear " + tourist.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(tourist.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "Your password has been changed successfully";
    }
    public List<Tourist> retrieveAllTourist() {
        return touristRepository.findAll();
    }
}
