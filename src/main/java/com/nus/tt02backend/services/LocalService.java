package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
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

    @Autowired
    PaymentService paymentService;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

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

    public Long createLocal(Local localToCreate) throws BadRequestException, StripeException {
        Local checkLocal = localRepository.retrieveLocalByEmail(localToCreate.getEmail());

        if (checkLocal != null) {
            throw new BadRequestException("The email address has been used to register another account, please enter another email");
        }

        localToCreate.setPassword(encoder.encode(localToCreate.getPassword()));
        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", localToCreate.getEmail());
        customer_parameters.put("name", localToCreate.getName());
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        localToCreate.setStripe_account_id(stripe_account_id);

        Map<String, Object> cardPayments =
                new HashMap<>();
        cardPayments.put("requested", true);
        Map<String, Object> transfers = new HashMap<>();
        transfers.put("requested", true);
        Map<String, Object> capabilities =
                new HashMap<>();
        capabilities.put("card_payments", cardPayments);
        capabilities.put("transfers", transfers);
        Map<String, Object> params = new HashMap<>();
        params.put("type", "custom");
        params.put("country", "SG");
        params.put("email", localToCreate.getEmail());
        Map<String, Object> company =
                new HashMap<>();
        company.put("name", localToCreate.getName());
        params.put("capabilities", capabilities);
        params.put("business_type", "individual");


        Account account = Account.create(params);

        localToCreate.setStripe_account_id(account.getId());

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
                local.setBooking_list(null);
                local.setPost_list(null);
                local.setComment_list(null);
                return local;

            } else {
                throw new EditUserException("Local not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public List<Local> retrieveAllLocal() {
        List<Local> localList = localRepository.findAll();

        for (Local l : localList) {
            l.setPassword(null);
            l.setBooking_list(null);
            l.setPost_list(null);
            l.setComment_list(null);
        }

        return localList;
    }
}
