package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.TouristRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.*;

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
        touristToCreate.setUser_type(UserTypeEnum.TOURIST);
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

    public List<Tourist> retrieveAllTourist() {
        return touristRepository.findAll();
    }

    public Tourist getTouristProfile(Long touristId) throws TouristNotFoundException {
        try {
            Tourist tourist = touristRepository.findById(touristId).get();

            if (tourist == null) {
                throw new TouristNotFoundException("Tourist not found!");
            }

            tourist.setPassword(null);

            return tourist;
        } catch(Exception ex) {
            throw new TouristNotFoundException(ex.getMessage());
        }
    }

    public Tourist editTouristProfile(Tourist touristToEdit) throws EditUserException {
        try {

            Optional<Tourist> touristOptional = touristRepository.findById(touristToEdit.getUser_id());

            if (touristOptional.isPresent()) {
                Tourist tourist = touristOptional.get();

                Long existingId = touristRepository.getTouristIdByEmail(touristToEdit.getEmail());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is an existing email
                    throw new EditUserException("Email currently in use. Please use a different email!");
                }

                existingId = touristRepository.getTouristIdByPassportNum(touristToEdit.getPassport_num());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is a passport number
                    throw new EditUserException("Passport number currently in use. Please use a different passport number!");
                }

                existingId = touristRepository.getTouristIdByMobileNum(touristToEdit.getMobile_num());
                if (existingId != null && existingId != touristToEdit.getUser_id()) { // but there is an existing mobile number
                    throw new EditUserException("Mobile number currently in use. Please use a different mobile number!");
                }

                tourist.setEmail(touristToEdit.getEmail());
                tourist.setName(touristToEdit.getName());
                tourist.setDate_of_birth(touristToEdit.getDate_of_birth());
                tourist.setCountry_code(touristToEdit.getCountry_code());
                tourist.setMobile_num(tourist.getMobile_num());
                touristRepository.save(tourist);
                tourist.setPassword(null);
                return tourist;

            } else {
                throw new EditUserException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public void editTouristPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<Tourist> touristOptional = touristRepository.findById(id);

            if (touristOptional.isPresent()) {
                Tourist tourist = touristOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, tourist.getPassword())) {
                    tourist.setPassword(encoder.encode(newPassword));
                    touristRepository.save(tourist);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}
