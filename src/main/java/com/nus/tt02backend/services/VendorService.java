package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Person;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VendorService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    PaymentService paymentService;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public Long createVendor(VendorStaff vendorStaffToCreate) throws BadRequestException, StripeException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(vendorStaffToCreate.getEmail());

        if (vendorStaff != null) {
            throw new BadRequestException("The email address has been used, please enter another email");
        }


        Vendor vendorToCreate = vendorStaffToCreate.getVendor();

//        Map<String, Object> cardPayments =
//                new HashMap<>();
//        cardPayments.put("requested", true);
//        Map<String, Object> transfers = new HashMap<>();
//        transfers.put("requested", true);
//        Map<String, Object> capabilities =
//                new HashMap<>();
//        capabilities.put("card_payments", cardPayments);
//        capabilities.put("transfers", transfers);
//        Map<String, Object> params = new HashMap<>();
//        params.put("type", "custom");
//        params.put("country", "SG");
//        params.put("email", vendorStaffToCreate.getEmail());
//        Map<String, Object> company =
//                new HashMap<>();
//        company.put("name", vendorToCreate.getBusiness_name());
//        Map<String, Object> company_address =
//                new HashMap<>();
//        company_address.put("line1", "Test test test");
//        company_address.put("postal_code", "12345");
//        company.put("address", company_address);
//        company.put("phone", "90909090");
//        company.put("tax_id", "12345678A");
//        Map<String, Object> business_profile =
//                new HashMap<>();
//
//        business_profile.put("mcc","7991");
//        business_profile.put("url","https://www.rwsentosa.com/en/attractions/universal-studios-singapore");
//
//        params.put("business_profile", business_profile);
//        company.put("owners_provided", true);
//        company.put("directors_provided", true);
//        Map<String, Object> verification =
//                new HashMap<>();
//        verification.put("document", "verified");
//        company.put("verification", verification);
//
//        // Representative
//        params.put("capabilities", capabilities);
//        params.put("business_type", "company");
//        params.put("company", company);
//        Map<String, Object> tosParams = new HashMap<>();
//        tosParams.put("date",  System.currentTimeMillis() / 1000L);
//        tosParams.put("ip", "8.8.8.8");
//        params.put("tos_acceptance", tosParams);
//
//
//
//        Account account = Account.create(params);
//
//        Map<String, Object> person_params = new HashMap<>();
//        person_params.put("first_name", "Jane");
//        person_params.put("last_name", "Diaz");
//        person_params.put("email", "alvinsiah@u.nus.edu");
//        person_params.put("nationality","SG");
//        Map<String, Object> person_address =
//                new HashMap<>();
//        person_address.put("line1", "Test test test");
//        person_address.put("postal_code", "12345");
//        person_params.put("id_number", "S1234567A");
//        Map<String, Object> dob_params = new HashMap<>();
//        dob_params.put("day",1L);
//        dob_params.put("month",1L);
//        dob_params.put("year",1990L);
//
//        Map<String, Object> relationship_params = new HashMap<>();
//        relationship_params.put("representative",true);
//        relationship_params.put("executive",true);
//        relationship_params.put("title","Team Lead");
//        person_params.put("relationship", relationship_params);
//        person_params.put("dob", dob_params);
//        person_params.put("phone","+6597314137");
//        person_params.put("address",person_address);
//        //Set alias to false
//
//
//
//
//        //account_params.put()
//
//
//        Person person = account.persons().create(person_params);
//
//        System.out.println(account.getRequirements());

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", vendorStaffToCreate.getEmail());
        customer_parameters.put("name", vendorToCreate.getBusiness_name());
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);

        vendorToCreate.setStripe_account_id(stripe_account_id);

        vendorRepository.save(vendorToCreate);

        vendorStaffToCreate.setPassword(encoder.encode(vendorStaffToCreate.getPassword()));
        String emailVerificationToken = UUID.randomUUID().toString();
        vendorStaffToCreate.setEmail_verification_token(emailVerificationToken);
        vendorStaffRepository.save(vendorStaffToCreate);

        String emailVerificationLink = "http://localhost:3001/verifyemail?token=" + vendorStaffToCreate.getEmail_verification_token();
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