package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.repositories.VendorRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeCreateParams;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
public class VendorStaffService {
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public void updateVendorStaff(VendorStaff vendorStaffToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = vendorStaffRepository.findById((vendorStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("VendorStaff not found"));

        if (vendorStaff.getEmail().equals(vendorStaffToUpdate.getEmail())) {
            if (vendorStaffToUpdate.getName() != null && !vendorStaffToUpdate.getName().isEmpty()) {
                vendorStaff.setName(vendorStaffToUpdate.getName());
            }
        }

        vendorStaffRepository.save(vendorStaff);
    }

    // not intial signup vendor staff, is subsequent one
    public Long createVendorStaff(VendorStaff vendorStaffToCreate) throws BadRequestException  {

        Long existingId = userRepository.retrieveIdByUserEmail(vendorStaffToCreate.getEmail());
        if (existingId != null) { // but there is an existing email
            throw new BadRequestException("Email currently in use. Please use a different email!");
        }

        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorStaffToCreate.getVendor().getVendor_id());

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            vendor.getVendor_staff_list().add(vendorStaffToCreate);
            vendorStaffToCreate.setVendor(vendor);
            String tempPassword = generateRandomPassword();
            vendorStaffToCreate.setPassword(encoder.encode(tempPassword));
            String emailVerificationToken = UUID.randomUUID().toString();
            vendorStaffToCreate.setEmail_verification_token(emailVerificationToken);
            vendorStaffToCreate.setEmail_verified(false);
            vendorStaffRepository.save(vendorStaffToCreate);

            String emailVerificationLink = "http://localhost:3001/verifyemail?token=" + vendorStaffToCreate.getEmail_verification_token();
            try {
                String subject = "[WithinSG] Account Application Processing";
                String content = "<p>Dear " + vendorStaffToCreate.getName() + ",</p>" +
                        "<p>Thank you for registering for a vendor account with WithinSG. " +
                        "We are glad that you have chosen us as your service provider!</p>" +
                        "<p>Your temporary password is " + tempPassword + "</p>" +
                        "<p>We have received your application and it is in the midst of processing. " +
                        "Please verify your email address by clicking on the button below.</p>" +
                        "<a href=\"" + emailVerificationLink +"\" target=\"_blank\">" +
                        "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                        "Verify Email</button></a>" +
                        "<p>An email will be sent to you once your account has been activated.</p>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(vendorStaffToCreate.getEmail(), subject, content);
                return vendorStaffToCreate.getUser_id();

            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
            }
        } else {
            throw new BadRequestException("Vendor is null!");
        }
    }

    public String generateRandomPassword() { // length 8, 7 letters & number, 1 symbol
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 8;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        generatedString = generatedString + "@";
        return generatedString;
    }

    public List<VendorStaff> getAllAssociatedVendorStaff(Long vendorId) {
        List<VendorStaff> vendorStaffs = vendorStaffRepository.getAllAssociatedVendorStaff(vendorId);

        for (VendorStaff vs : vendorStaffs) {
            vs.setPost_list(null);
            vs.setComment_list(null);
            vs.getVendor().setVendor_staff_list(null);
            vs.setIncoming_support_ticket_list(null);
            vs.setOutgoing_support_ticket_list(null);
        }

        return vendorStaffs;
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        String passwordResetToken = UUID.randomUUID().toString();
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);

        if (vendorStaff == null) {
            throw new BadRequestException("There is no vendor staff account associated with this email address");
        }

        vendorStaff.setPassword_reset_token(passwordResetToken);
        vendorStaff.setPassword_token_date(LocalDateTime.now());
        vendorStaffRepository.save(vendorStaff);
        String passwordResetLink = "http://localhost:3001/passwordreset?token=" + vendorStaff.getPassword_reset_token();
        try {
            String subject = "[WithinSG] Password Reset Instructions";
            String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>You can reset your password by clicking on the button below: </p>" +
                    "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                    "Reset Password</button></a>" +
                    "<p>Note that the link will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(vendorStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String token, String password) throws BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByPasswordToken(token);

        if (vendorStaff == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(vendorStaff.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        vendorStaff.setPassword(encoder.encode(password));
        vendorStaff.setPassword_reset_token(null);
        vendorStaff.setPassword_token_date(null);
        vendorStaffRepository.save(vendorStaff);

        try {
            String subject = "[WithinSG] Password Reset Successfully";
            String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(vendorStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "Your password has been changed successfully";
    }

    public String verifyEmail(String token) throws BadRequestException {
        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmailToken(token);

        if (vendorStaff == null) {
            throw new BadRequestException("Invalid token");
        }
        System.out.println("tan: " + vendorStaff.getEmail_verified());

        if (vendorStaff.getEmail_verified() == null || !vendorStaff.getEmail_verified()) {
            vendorStaff.setEmail_verified(true);
            vendorStaffRepository.save(vendorStaff);

            try {
                String subject = "[WithinSG] Email Verified Successfully";
                String content = "<p>Dear " + vendorStaff.getName() + ",</p>" +
                        "<p>Your email has been verified successfully." +
                        "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(vendorStaff.getEmail(), subject, content);
            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the successful email verification email");
            }
        }

        return "Your email has been verified successfully";
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public VendorStaff editVendorStaffProfile(VendorStaff vendorStaffToEdit) throws BadRequestException {
        try {

            Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(vendorStaffToEdit.getUser_id());

            if (vendorStaffOptional.isPresent()) {
                VendorStaff vendorStaff = vendorStaffOptional.get();

                Long existingId = vendorStaffRepository.getVendorStaffIdByEmail(vendorStaffToEdit.getEmail());
                if (existingId != null && existingId != vendorStaffToEdit.getUser_id()) { // but there is an existing email
                    throw new BadRequestException("Email currently in use. Please use a different email!");
                }

                vendorStaff.setEmail(vendorStaffToEdit.getEmail());
                vendorStaff.setName(vendorStaffToEdit.getName());
                vendorStaff.setPosition(vendorStaffToEdit.getPosition());
                vendorStaffRepository.save(vendorStaff);

                if (vendorStaff.getIs_master_account()) {
                    Vendor vendor = vendorStaff.getVendor();
                    vendor.setBusiness_name(vendorStaffToEdit.getVendor().getBusiness_name());
                    vendor.setPoc_name(vendorStaffToEdit.getVendor().getPoc_name());
                    vendor.setPoc_position(vendorStaffToEdit.getVendor().getPoc_position());
                    vendor.setCountry_code(vendorStaffToEdit.getVendor().getCountry_code());
                    vendor.setPoc_mobile_num(vendorStaffToEdit.getVendor().getPoc_mobile_num());
                    vendor.setService_description(vendorStaffToEdit.getVendor().getService_description());
                    vendorRepository.save(vendor);
                }

                vendorStaff.getVendor().setVendor_staff_list(null);
                return vendorStaff;

            } else {
                throw new BadRequestException("Vendor staff not found!");
            }
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public void toggleBlock(Long vendorStaffId) throws NotFoundException, ToggleBlockException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(vendorStaffId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            if (vendorStaff.getIs_master_account() && !vendorStaff.getIs_blocked()) { // master vendor staff
                throw new ToggleBlockException("Vendor Staff access rights prevents him from being blocked!");
            }
            vendorStaff.setIs_blocked(!vendorStaff.getIs_blocked());
            vendorStaffRepository.save(vendorStaff);

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }
    }

    public List<VendorStaff> retrieveAllVendorStaff() {
        List<VendorStaff> vendorStaffList = vendorStaffRepository.findAll();

        for (VendorStaff i : vendorStaffList) {
            i.setPassword(null);
            i.setPost_list(null);
            i.setComment_list(null);
            i.getVendor().setVendor_staff_list(null);
            i.setIncoming_support_ticket_list(null);
            i.setOutgoing_support_ticket_list(null);
        }

        return vendorStaffList;
    }


    public String addBankAccount(Long userId, String token) throws NotFoundException, StripeException, BadRequestException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(userId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            Vendor vendor = vendorStaff.getVendor();

            String stripe_account_id = vendor.getStripe_account_id();

            Map<String, Object> retrieveParams =
                    new HashMap<>();
            List<String> expandList = new ArrayList<>();
            expandList.add("sources");
            retrieveParams.put("expand", expandList);
            Customer customer =
                    Customer.retrieve(
                            stripe_account_id,
                            retrieveParams,
                            null
                    );

            Token bankAccountToken = Token.retrieve(token);

            BankAccount bankAccountFromToken = (BankAccount) bankAccountToken.getBankAccount();
            String last4FromToken = bankAccountFromToken.getLast4();
            String bankNameFromToken = bankAccountFromToken.getBankName();

            List<PaymentSource> externalAccounts = customer.getSources().getData();
            for (PaymentSource externalAccount : externalAccounts) {
                if (externalAccount instanceof BankAccount) {
                    BankAccount existingBankAccount = (BankAccount) externalAccount;
                    if (existingBankAccount.getLast4().equals(last4FromToken)) {

                        throw new BadRequestException("Cannot add existing bank account!");
                    }
                }
            }

            Map<String, Object> params = new HashMap<>();
            params.put(
                    "source",
                    token
            );

            BankAccount bankAccount =
                    (BankAccount) customer.getSources().create(
                            params
                    );

//            List<Integer> amounts = new ArrayList<>();
//            amounts.add(32);
//            amounts.add(45);
//            Map<String, Object> verification_params = new HashMap<>();
//            verification_params.put("amounts", amounts);
//
//            BankAccount updatedBankAccount =
//                    (BankAccount) bankAccount.verify(verification_params);


            return bankAccount.getId();

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }

    }

    public String deleteBankAccount(Long userId, String bank_account_id) throws NotFoundException, StripeException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(userId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            Vendor vendor = vendorStaff.getVendor();

            String stripe_account_id = vendor.getStripe_account_id();

            Map<String, Object> retrieveParams =
                    new HashMap<>();
            List<String> expandList = new ArrayList<>();
            expandList.add("sources");
            retrieveParams.put("expand", expandList);
            Customer customer =
                    Customer.retrieve(
                            stripe_account_id,
                            retrieveParams,
                            null
                    );

            BankAccount bankAccount =
                    (BankAccount) customer.getSources().retrieve(
                            bank_account_id
                    );


            BankAccount deletedBankAccount =
                    bankAccount.delete();

            return deletedBankAccount.getId();

        } else {
            throw new NotFoundException("Vendor not found!");
        }

    }


    public List<PaymentSource> getBankAccounts(Long userId) throws NotFoundException, StripeException {
        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(userId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            Vendor vendor = vendorStaff.getVendor();

            String stripe_account_id = vendor.getStripe_account_id();

            Map<String, Object> retrieveParams =
                    new HashMap<>();
            List<String> expandList = new ArrayList<>();
            expandList.add("sources");
            retrieveParams.put("expand", expandList);
            Customer customer =
                    Customer.retrieve(
                            stripe_account_id,
                            retrieveParams,
                            null
                    );

            Map<String, Object> params = new HashMap<>();
            params.put("object", "bank_account");


            PaymentSourceCollection bankAccounts =
                    customer.getSources().list(params);

            List<PaymentSource> currentBankAccounts = bankAccounts.getData();

            return currentBankAccounts;

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }
    }

    public BigDecimal withdrawWallet(Long userId, String bank_account_id, BigDecimal amount) throws StripeException, NotFoundException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(userId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            Vendor vendor = vendorStaff.getVendor();

            String stripe_account_id = vendor.getStripe_account_id();

            Customer customer =
                    Customer.retrieve(stripe_account_id);

            Map<String, Object> params = new HashMap<>();
            params.put("amount", -amount.multiply(new BigDecimal("100")).intValueExact());
            params.put("currency", "sgd");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("transaction_type", "Withdrawals");
            params.put("metadata", metadata);

            CustomerBalanceTransaction balanceTransaction =
                    customer.balanceTransactions().create(params);

            BigDecimal currentWalletBalance = vendor.getWallet_balance();

            vendor.setWallet_balance(currentWalletBalance.subtract(amount));


            vendorRepository.save(vendor);

            BigDecimal newWalletBalance = vendor.getWallet_balance();

            return newWalletBalance;

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }


    }

    public BigDecimal topUpWallet(Long userId, String bank_account_id, BigDecimal amount) throws StripeException, NotFoundException {

        Optional<VendorStaff> vendorStaffOptional = vendorStaffRepository.findById(userId);

        if (vendorStaffOptional.isPresent()) {
            VendorStaff vendorStaff = vendorStaffOptional.get();

            Vendor vendor = vendorStaff.getVendor();

            String stripe_account_id = vendor.getStripe_account_id();

            Map<String, Object> automaticPaymentMethods =
                    new HashMap<>();
            automaticPaymentMethods.put("enabled", true);

            Map<String, Object> paymentParams = new HashMap<>();
            paymentParams.put("amount", amount.multiply(new BigDecimal("100")).intValueExact());
            paymentParams.put("currency", "sgd");
            paymentParams.put(
                    "automatic_payment_methods",
                    automaticPaymentMethods
            );
            paymentParams.put(
                    "confirm",
                    true
            );
            paymentParams.put(
                    "customer",
                    stripe_account_id
            );

            paymentParams.put(
                    "payment_method",
                    bank_account_id
            );

            paymentParams.put(
                    "return_url",
                    "yourappname://stripe/callback"
            );



            PaymentIntent paymentIntent =
                    PaymentIntent.create(paymentParams);


            vendor.setWallet_balance(vendor.getWallet_balance().add(amount));

            vendorRepository.save(vendor);

            BigDecimal newWalletBalance = vendor.getWallet_balance();


            return newWalletBalance;

        } else {
            throw new NotFoundException("Vendor Staff not found!");
        }


    }
}

