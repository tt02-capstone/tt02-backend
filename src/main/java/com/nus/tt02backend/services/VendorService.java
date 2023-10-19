package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.TicketPerDay;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

    public List<Vendor> getAllVendors() {
        List<Vendor> vendorList = vendorRepository.findAll();

        for (Vendor v : vendorList) {
            v.setAttraction_list(null);
            v.setAccommodation_list(null);
            v.setRestaurant_list(null);
            v.setTelecom_list(null);
            v.setDeals_list(null);
            v.setVendor_staff_list(null);
            v.setStripe_account_id(null);
        }

        return vendorList;
    }

    public BigDecimal updateWallet(Long vendorId, BigDecimal updateAmount) throws BadRequestException, NotFoundException, StripeException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            String stripe_account_id = vendor.getStripe_account_id();

            Customer customer =
                    Customer.retrieve(stripe_account_id);


            BigDecimal updatedWalletBalance = vendor.getWallet_balance().add(updateAmount);
            if (updatedWalletBalance.compareTo(BigDecimal.ZERO) >= 0) {
                vendor.setWallet_balance(updatedWalletBalance);
                vendorRepository.save(vendor);
                Map<String, Object> params = new HashMap<>();
                params.put("amount", updateAmount.multiply(new BigDecimal("100")).intValueExact());
                params.put("currency", "sgd");
                Map<String, Object> metadata = new HashMap<>();

                if (updateAmount.signum() > 0) {
                    metadata.put("transaction_type", "Manual Credit");
                } else {
                    metadata.put("transaction_type", "Manual Debit");
                }

                params.put("metadata", metadata);
                CustomerBalanceTransaction balanceTransaction =
                        customer.balanceTransactions().create(params);
                return updatedWalletBalance;
            } else {
                throw new BadRequestException("Insufficient wallet balance to deduct from");
            }

        } else {
            throw new NotFoundException("Vendor does not exist");
        }

    }

    public List<HashMap<String, Object>> getWithdrawalRequests(Long vendorId) throws NotFoundException, StripeException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            Customer customer =
                    Customer.retrieve(vendor.getStripe_account_id());

            Map<String, Object> params = new HashMap<>();


            CustomerBalanceTransactionCollection balanceTransactions =
                    customer.balanceTransactions().list(params);

            List<HashMap<String, Object>> extractedData = new ArrayList<>();

            for (CustomerBalanceTransaction transaction : balanceTransactions.getData()) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", transaction.getId());
                BigDecimal amount = new BigDecimal(transaction.getAmount()).divide(new BigDecimal("100.0"));
                map.put("amount", amount);

                // Converting Unix timestamp to formatted date-time string
                long timestamp = transaction.getCreated();
                Date date = new Date(timestamp * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String formattedDate = sdf.format(date);

                // Splitting the formattedDate into date and time
                String[] dateTimeParts = formattedDate.split(" ");
                map.put("date", dateTimeParts[0]);
                map.put("time", dateTimeParts[1]);

                map.put("type", transaction.getMetadata().get("transaction_type"));

                extractedData.add(map);
            }



            return extractedData;

        } else {
            throw new NotFoundException("Vendor does not exist");
        }
    }
}