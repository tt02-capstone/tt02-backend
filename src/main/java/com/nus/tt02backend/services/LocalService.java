package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.Customer;
import com.stripe.model.CustomerBalanceTransaction;
import com.stripe.model.CustomerBalanceTransactionCollection;
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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
//        params.put("email", localToCreate.getEmail());
//        Map<String, Object> company =
//                new HashMap<>();
//        company.put("name", localToCreate.getName());
//        params.put("capabilities", capabilities);
//        params.put("business_type", "individual");
//
//
//        Account account = Account.create(params);
//
//        localToCreate.setStripe_business_id(account.getId());


        localToCreate.setUser_type(UserTypeEnum.LOCAL);
        UUID uuid = UUID.randomUUID();
        long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
        String emailVerificationToken =  String.format("%04d", otpValue);
        localToCreate.setEmail_verification_token(emailVerificationToken);
        localToCreate.setEmail_verified(false);
        localToCreate.setToken_date(LocalDateTime.now());
        localRepository.save(localToCreate);

        try {
            String subject = "[WithinSG] User Account Application Processing";
            String content = "<p>Dear " + localToCreate.getName() + ",</p>" +
                    "<p>Thank you for registering for an account with WithinSG. " +
                    "We are glad that you have chosen us to help you explore Singapore!</p>" +
                    "<p>Please enter your code into the WithinSG application to verify your email: </p>" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" + emailVerificationToken + "</button></a>" +
                    "<p>Note that the code will expire after 60 minutes.</p>" +
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

    public Local editLocalProfile(Local localToEdit) throws BadRequestException {
        try {

            Optional<Local> localOptional = localRepository.findById(localToEdit.getUser_id());

            if (localOptional.isPresent()) {
                Local local = localOptional.get();

                Long existingId = localRepository.getLocalIdByEmail(localToEdit.getEmail());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is an existing email
                    throw new BadRequestException("Email currently in use. Please use a different email!");
                }

                existingId = localRepository.getLocalIdByNRICNum(local.getNric_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a NRIC number
                    throw new BadRequestException("NRIC number currently in use. Please use a different NRIC number!");
                }

                existingId = localRepository.getLocalIdByMobileNum(localToEdit.getMobile_num());
                if (existingId != null && existingId != localToEdit.getUser_id()) { // but there is a mobile number
                    throw new BadRequestException("Mobile number currently in use. Please use a different mobile number!");
                }

                local.setEmail(localToEdit.getEmail());
                local.setName(localToEdit.getName());
                local.setDate_of_birth(localToEdit.getDate_of_birth());
                local.setCountry_code(localToEdit.getCountry_code());
                local.setMobile_num(localToEdit.getMobile_num());
                localRepository.save(local);
                local.setBooking_list(null);
                local.setPost_list(null);
                local.setComment_list(null);
                return local;

            } else {
                throw new BadRequestException("Local not found!");
            }
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
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

    public BigDecimal updateWallet(Long localId, BigDecimal updateAmount) throws BadRequestException, NotFoundException, StripeException {

        Optional<Local> localOptional = localRepository.findById(localId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();
            String stripe_account_id = local.getStripe_account_id();

            Customer customer =
                    Customer.retrieve(stripe_account_id);


            BigDecimal updatedWalletBalance = local.getWallet_balance().add(updateAmount);
            if (updatedWalletBalance.compareTo(BigDecimal.ZERO) >= 0) {
                local.setWallet_balance(updatedWalletBalance);
                localRepository.save(local);
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
            throw new NotFoundException("Local does not exist");
        }
    }

    public List<HashMap<String, Object>> getWithdrawalRequests(Long localId) throws StripeException, NotFoundException {

        Optional<Local> localOptional = localRepository.findById(localId);

        if (localOptional.isPresent()) {
            Local local = localOptional.get();
            Customer customer =
                    Customer.retrieve(local.getStripe_account_id());

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
