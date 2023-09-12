package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.LocalRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LocalService {
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
}
