package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.*;

@Service
public class LocalService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Local localLogin(String email, String password) throws NotFoundException, BadRequestException {
        Local checkLocal = localRepository.retrieveLocalByEmail(email);

        if (checkLocal == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkLocal.getPassword())
                && !checkLocal.getIs_blocked()) {
            //still need to add more
            checkLocal.setComment_list(new ArrayList<>());
            checkLocal.setPost_list(null);
            checkLocal.setBadge_list(null);
            checkLocal.setSupport_ticket_list(null);
            checkLocal.setAttraction_list(null);
            checkLocal.setPost_list(null);
            checkLocal.setAccommodation_list(null);
            checkLocal.setCard_list(null);
            checkLocal.setCart_list(null);
            checkLocal.setRestaurant_list(null);
            checkLocal.setTelecom_list(null);
            checkLocal.setTour_type_list(null);
            checkLocal.setItinerary(null);
            return checkLocal;

        } else if (checkLocal.getIs_blocked()) {
            throw new BadRequestException("Your account is disabled, please contact our help desk");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public void updateLocal(Local localToUpdate) throws NotFoundException {
        Local local = localRepository.findById(localToUpdate.getUser_id()).orElseThrow(() ->
                new NotFoundException("Local not found"));

        if (local.getEmail().equals(localToUpdate.getEmail())) {
            if (localToUpdate.getName() != null && !localToUpdate.getName().isEmpty()) {
                local.setName(localToUpdate.getName());
            }
        }

        localRepository.save(local);
    }

    public Long createLocal(Local localToCreate) throws BadRequestException {
        Local checkLocal = localRepository.retrieveLocalByEmail(localToCreate.getEmail());

        if (checkLocal != null) {
            throw new BadRequestException("The email address has been used to register another account, please enter another email");
        }

        localToCreate.setPassword(encoder.encode(localToCreate.getPassword()));
        localToCreate.setUser_type(UserTypeEnum.LOCAL);
        localRepository.save(localToCreate);

        try {
            String subject = "[WithinSG] User Account Application Processing";
            String content = "<p>Dear " + localToCreate.getName() + ",</p>" +
                    "<p>Thank you for registering for an account with WithinSG. " +
                    "We are glad that you have chosen us to help you explore Singapore!</p>" +
                    "<p>We have received your application and it is in the midst of processing.</p>" +
                    "<p>An email will be sent to you once your account has been activated.</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(localToCreate.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return localToCreate.getUser_id();
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);;
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }
    public List<Local> retrieveAllLocal() {
        return localRepository.findAll();
    }

    public Local getLocalProfile(Long localId) throws LocalNotFoundException {
        try {
            Local local = localRepository.findById(localId).get();

            if (local == null) {
                throw new LocalNotFoundException("Local not found!");
            }

            local.setPassword(null);

            return local;
        } catch(Exception ex) {
            throw new LocalNotFoundException(ex.getMessage());
        }
    }

    public Local editLocalProfile(Local localToEdit) throws EditUserException {
        try {

            Optional<Local> localOptional = localRepository.findById(localToEdit.getUser_id());

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                Long existingId = localRepository.getLocalIdByEmail(localToEdit.getEmail());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is an existing email
                    throw new EditUserException("Email currently in use. Please use a different email!");
                }

                existingId = localRepository.getLocalIdByNRICNum(local.getNric_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a NRIC number
                    throw new EditUserException("NRIC number currently in use. Please use a different NRIC number!");
                }

                existingId = localRepository.getLocalIdByMobileNum(localToEdit.getMobile_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a mobile number
                    throw new CreateLocalException("Mobile number currently in use. Please use a different mobile number!");
                }

                local.setEmail(localToEdit.getEmail());
                local.setName(localToEdit.getName());
                local.setDate_of_birth(localToEdit.getDate_of_birth());
                local.setCountry_code(localToEdit.getCountry_code());
                local.setMobile_num(localToEdit.getMobile_num());
                localRepository.save(local);
                local.setPassword(null);
                return local;

            } else {
                throw new EditUserException("Local not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public void editLocalPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<Local> localOptional = localRepository.findById(id);

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, local.getPassword())) {
                    local.setPassword(encoder.encode(newPassword));
                    localRepository.save(local);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("Local not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}
