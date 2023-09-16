package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.time.*;

@Service
public class InternalStaffService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    InternalStaffRepository internalStaffRepository;
    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    JavaMailSender javaMailSender;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public InternalStaff staffLogin(String email, String password) throws NotFoundException, BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new NotFoundException("There is no staff account associated with this email address");
        }

        if (encoder.matches(password, internalStaff.getPassword())
                && !internalStaff.getIs_blocked()) {
            internalStaff.setComment_list(null);
            internalStaff.setPost_list(null);
            internalStaff.setBadge_list(null);
            internalStaff.setSupport_ticket_list(null);
            return internalStaff;
        } else if (internalStaff.getIs_blocked()) {
            throw new BadRequestException("Your staff account is disabled, please contact your administrator");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public void updateStaff(InternalStaff internalStaffToUpdate) throws NotFoundException {
        InternalStaff internalStaff = internalStaffRepository.findById((internalStaffToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("InternalStaff not found"));

        if (internalStaff.getEmail().equals(internalStaffToUpdate.getEmail())) {
            if (internalStaffToUpdate.getUser_id() != null && !internalStaffToUpdate.getName().isEmpty()) {
                internalStaff.setName(internalStaffToUpdate.getName());
            }
        }

        internalStaffRepository.save(internalStaff);
    }

    public Long createStaff(InternalStaff internalStaffToCreate) throws BadRequestException {

        Long existingId = internalStaffRepository.getAdminByEmail(internalStaffToCreate.getEmail());
        if (existingId != null && existingId != internalStaffToCreate.getUser_id()) { // but there is an existing email
            throw new BadRequestException("Email currently in use. Please use a different email!");
        }

        Long latestStaffNum = internalStaffRepository.getLatestStaffNum();
        if (latestStaffNum == null || latestStaffNum < 1L) {
            latestStaffNum = 1L;
        } else {
            latestStaffNum++;
        }

        internalStaffToCreate.setStaff_num(latestStaffNum);
        String tempPassword = generateRandomPassword();
        System.out.println("twk: " + tempPassword);
        internalStaffToCreate.setPassword(encoder.encode(tempPassword));
        internalStaffRepository.save(internalStaffToCreate);

        try {
            String subject = "[WithinSG] Staff Account Created";
            String content = "<p>Dear " + internalStaffToCreate.getName() + ",</p>" +
                    "<p>A staff account has been created for you.</p>" +
                    "<p>Your temporary password is " + tempPassword + "</p>" +
                    "<p>Please sign in using your staff number as the password. " +
                    "You will be prompted to change your password upon signing in for the first time.</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaffToCreate.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return internalStaffToCreate.getUser_id();
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

        generatedString = generatedString + "!";
        return generatedString;
    }

    public List<InternalStaff> retrieveAllAdmin() {
        List<InternalStaff> internalStaffList = internalStaffRepository.findAll();

        for (InternalStaff i : internalStaffList) {
            i.setPassword(null);
        }

        return internalStaffList;
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        UUID uuid = UUID.randomUUID();
        long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
        String passwordResetOTP =  String.format("%04d", otpValue);

        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        internalStaff.setPassword_reset_token(passwordResetOTP);
        internalStaff.setPassword_token_date(LocalDateTime.now());
        internalStaffRepository.save(internalStaff);

        String passwordResetLink = "http://localhost:3000/passwordreset";
        try {
            String subject = "[WithinSG] Your Password Reset Instructions";
            String content = "<p>Dear " + internalStaff.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>Please enter your verification code <b>" + internalStaff.getPassword_reset_token() + "</b> into the WithinSG platform: </p>" +
                    "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">Reset Password</button></a>" +
                    "<p>Note that the code will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String email, String token) throws BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new BadRequestException("Invalid email");
        }

        if (!internalStaff.getPassword_reset_token().equals(token)) {
            throw new BadRequestException("Invalid OTP");
        }

        if (Duration.between(internalStaff.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            UUID uuid = UUID.randomUUID();
            long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
            String passwordResetOTP =  String.format("%04d", otpValue);

            internalStaff.setPassword_reset_token(passwordResetOTP);
            internalStaff.setPassword_token_date(LocalDateTime.now());
            internalStaffRepository.save(internalStaff);

            String passwordResetLink = "http://localhost:3000/passwordreset";
            try {
                String subject = "[WithinSG] Your Password Reset Instructions";
                String content = "<p>Dear " + internalStaff.getName() + ",</p>" +
                        "<p>A request was received to reset the password for your account." +
                        "<p>Please enter your verification code <b>" + internalStaff.getPassword_reset_token() + "</b> into the WithinSG platform: </p>" +
                        "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                        "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">Reset Password</button></a>" +
                        "<p>Note that the code will expire after 60 minutes.</p>" +
                        "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(internalStaff.getEmail(), subject, content);
            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
            }

            throw new BadRequestException("Your OTP has expired, a new OTP has been sent to your email");
        }

        return "The code is verified correctly";
    }

    public String passwordResetStageThree(String email, String password) throws BadRequestException {
        InternalStaff internalStaff = internalStaffRepository.retrieveInternalStaffByEmail(email);

        if (internalStaff == null) {
            throw new BadRequestException("Invalid email");
        }

        internalStaff.setPassword(encoder.encode(password));
        internalStaff.setPassword_reset_token(null);
        internalStaff.setPassword_token_date(null);
        internalStaffRepository.save(internalStaff);

        try {
            String subject = "[WithinSG] Your Password was reset Successfully";
            String content = "<p>Dear " + internalStaff.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(internalStaff.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "Your password has been changed successfully";
    }

    public List<Vendor> getPendingApplications() {
        List<Vendor> vendors = vendorRepository.retrievePendingVendorApplications(ApplicationStatusEnum.PENDING);
        for (Vendor vendor : vendors) {
            vendor.setVendor_staff_list(null);
        }

        return vendors;
    }

    public String updateApplicationStatus(Long vendorId, ApplicationStatusEnum applicationStatus) throws NotFoundException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
        if (vendorOptional.isEmpty()) {
            throw new NotFoundException("Invalid Vendor ID");
        }

        Vendor vendor = vendorOptional.get();
        vendor.setApplication_status(applicationStatus);
        vendorRepository.save(vendor);

        return "Application status updated successfully";
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public InternalStaff editProfile(InternalStaff staffToEdit) throws EditAdminException {
        try {

            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(staffToEdit.getUser_id());

            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();

                Long existingId = userRepository.retrieveIdByUserEmail(staffToEdit.getEmail());
                if (existingId != null && existingId != staffToEdit.getUser_id()) { // but there is an existing email
                    throw new EditAdminException("Email currently in use. Please use a different email!");
                }

                internalStaff.setEmail(staffToEdit.getEmail());
                internalStaff.setName(staffToEdit.getName());
                internalStaffRepository.save(internalStaff);
                internalStaff.setPassword(null);
                return internalStaff;

            } else {
                throw new EditAdminException("Admin staff not found!");
            }
        } catch (Exception ex) {
            throw new EditAdminException(ex.getMessage());
        }
    }
}
