package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
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
                tourist.setMobile_num(touristToEdit.getMobile_num());
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

    public List<Tourist> retrieveAllTourist() {
        List<Tourist> touristList = touristRepository.findAll();

        for (Tourist i : touristList) {
            i.setPassword(null);
        }

        return touristList;
    }
}
