package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
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
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    LocalRepository localRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

//    public User vendorPortalLogin(String email, String password) throws NotFoundException, BadRequestException {
//        VendorStaff vendorStaff = vendorStaffRepository.retrieveVendorStaffByEmail(email);
//
//        if (vendorStaff != null) {
//            if (encoder.matches(password, vendorStaff.getPassword())
//                    && !vendorStaff.getIs_blocked()
//                    && vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.APPROVED) {
//                vendorStaff.getVendor().setWithdrawal_list(null);
//                vendorStaff.getVendor().setVendor_staff_list(null);
//                vendorStaff.setComment_list(null);
//                vendorStaff.getVendor().setPost_list(null);
//                vendorStaff.getVendor().setAttraction_list(null);
//                vendorStaff.getVendor().setAccommodation_list(null);
//                vendorStaff.getVendor().setRestaurant_list(null);
//                vendorStaff.getVendor().setTelecom_list(null);
//                vendorStaff.getVendor().setDeals_list(null);
//                return vendorStaff;
//            } else if (vendorStaff.getIs_blocked()) {
//                throw new BadRequestException("Your staff account is disabled, please contact your administrator!");
//            }
//            else if (vendorStaff.getVendor().getApplication_status() != ApplicationStatusEnum.APPROVED) {
//                throw new BadRequestException("Your application is still pending review!");
//            }
//            else {
//                throw new BadRequestException("Incorrect password!");
//            }
//        }
//
////        Local local = localRepository.retrieveLocalByEmail(email);
//
////        if (local != null) {
////            return local;
////        }
//        return null;
//
////        throw new NotFoundException("There is no account associated with this email address!");
//    }
//
//    // for all 4 inheritance type
    public User getUserProfile(Long id) throws IllegalArgumentException, UserNotFoundException {
        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(null);

                if (user instanceof VendorStaff) {
                    VendorStaff vendorStaff = (VendorStaff) user;
                    vendorStaff.getVendor().setVendor_staff_list(null);
                    return vendorStaff;
                } else if (user instanceof Tourist) {
                    Tourist tourist = (Tourist) user;
                    tourist.setBooking_list(null);
                    tourist.setComment_list(null);
                } else if (user instanceof Local) {
                    Local local = (Local) user;
                    local.setBooking_list(null);
                    local.setComment_list(null);
                } else if (user instanceof InternalStaff) {
                    InternalStaff internalStaff = (InternalStaff) user;
                    internalStaff.setComment_list(null);
                }

                return user;

            } else {
                throw new UserNotFoundException("User not found!");
            }

        } catch(Exception ex) {
            throw new UserNotFoundException(ex.getMessage());
        }
    }
//
//    // only for vendor, locals and tourist
//    public User editUserProfile(User userToEdit) throws EditUserException {
//        try {
//            System.out.println(userToEdit);
//            return null;
////            Optional<User> userOptional = userRepository.findById(userToEdit.getUser_id());
////
////            if (userOptional.isPresent()) {
////                User user = userOptional.get();
////                // email handling, common attribute among all users
////                Long existingId = userRepository.getUserIdByEmail(userToEdit.getEmail());
////                if (existingId != null && existingId != userToEdit.getUser_id()) { // email exist,and is not current user email
////                    throw new EditUserException("Email currently in use. Please use a different email!");
////                }
////
////                if (userToEdit.getUser_type() == UserTypeEnum.VENDOR_STAFF) {
////                    VendorStaff vendorStaff = (VendorStaff) user;
////                    vendorStaff.setEmail(vendorStaffToEdit.getEmail());
////                    vendorStaff.setName(vendorStaffToEdit.getName());
////                    vendorStaff.setPosition(vendorStaffToEdit.getPosition());
////                    System.out.println("vendor saved");
////                    userRepository.save(vendorStaff);
//////                    vendorStaffRepository.save(vendorStaff);
////                    vendorStaff.setVendor(null);
////                    vendorStaff.setComment_list(null);
////                    vendorStaff.setPassword(null);
////                    return vendorStaff;
////                } else {
////                    return null;
////                }
//
////            } else {
////                throw new UserNotFoundException("User not found!");
////            }
//
//        } catch (Exception ex) {
//            throw new EditUserException(ex.getMessage());
//        }
//    }
//
//    public void editUserPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
//        try {
//            Optional<User> userOptional = userRepository.findById(id);
//
//            if (userOptional.isPresent()) {
//                User user = userOptional.get();
//
//                if (oldPassword.equals(newPassword)) {
//                    throw new EditPasswordException("New password must be different from old password!");
//
//                } else if (encoder.matches(oldPassword, user.getPassword())) {
//                    user.setPassword(encoder.encode(newPassword));
//                    userRepository.save(user);
//
//                } else {
//                    throw new EditPasswordException("Incorrect old password!");
//                }
//
//            } else {
//                throw new EditUserException("User not found!");
//            }
//        } catch (Exception ex) {
//            throw new EditPasswordException(ex.getMessage());
//        }
//    }
    public User userMobileLogin(String email, String password) throws NotFoundException, BadRequestException {
        User checkUser = userRepository.retrieveTouristOrLocalByEmail(email);
        System.out.println(checkUser);
        if (checkUser == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkUser.getPassword())
                && !checkUser.getIs_blocked()) {
            //can check and initialise here foreign keys here
            return checkUser;

        } else if (checkUser.getIs_blocked()) {
            throw new BadRequestException("Your account is disabled, please contact our help desk");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public User userWebLogin(String email, String password) throws NotFoundException, BadRequestException {
        User checkUser = userRepository.retrieveVendorStaffOrLocalByEmail(email);

        if (checkUser == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkUser.getPassword())
                && !checkUser.getIs_blocked()) {
            if (checkUser.getUser_type() == UserTypeEnum.VENDOR_STAFF) {
                VendorStaff vendorStaff = (VendorStaff) checkUser;

                if (checkUser.getEmail_verified() &&
                        vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.APPROVED) {
                    return checkUser;
                } else if (!checkUser.getEmail_verified()) {
                    String emailVerificationLink = "http://localhost:3000/verifyemail?token=" + checkUser.getEmail_verification_token();
                    try {
                        String subject = "[WithinSG] Email Verification Required";
                        String content = "<p>Dear " + checkUser.getName() + ",</p>" +
                                "<p>You have not verified your email address.</p>" +
                                "<p>Please verify your email address by clicking on the button below.</p>" +
                                "<a href=\"" + emailVerificationLink +"\" target=\"_blank\">" +
                                "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" +
                                "Verify Email</button></a>" +
                                "<p>Kind Regards,<br> WithinSG</p>";
                        sendEmail(checkUser.getEmail(), subject, content);
                    } catch (MessagingException ex) {
                        throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
                    }

                    throw new BadRequestException("Your email has not been verified, a new email verification email has been sent to you");
                } else {
                    throw new BadRequestException("Your application is still pending review");
                }
            }

            return checkUser;
        } else if (checkUser.getIs_blocked()) {
            throw new BadRequestException("Your account is disabled, please contact our help desk");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public void updateUser(User userToUpdate) throws NotFoundException {
        User user = userRepository.findById((userToUpdate.getUser_id()))
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getEmail().equals(userToUpdate.getEmail())) {
            if (userToUpdate.getName() != null && !userToUpdate.getName().isEmpty()) {
                user.setName(userToUpdate.getName());
            }
        }

        userRepository.save(user);
    }

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);;
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public String passwordResetStageOne(String email) throws BadRequestException {
        UUID uuid = UUID.randomUUID();
        long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
        String passwordResetOTP =  String.format("%04d", otpValue);

        User user = userRepository.retrieveTouristOrLocalByEmail(email);

        if (user == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        user.setPassword_reset_token(passwordResetOTP);
        user.setPassword_token_date(LocalDateTime.now());
        userRepository.save(user);

        try {
            String subject = "[WithinSG] Your Password Reset Instructions";
            String content = "<p>Dear " + user.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>Please enter your vertification code into the WithinSG application: </p>" +
                    "<a href=\"" + passwordResetOTP +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">" + passwordResetOTP + "</button></a>" +
                    "<p>Note that the code will expire after 60 minutes.</p>" +
                    "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "You will receive an email containing the instructions to reset your password.";
    }

    public String passwordResetStageTwo(String token) throws BadRequestException {
        System.out.println(token);
        User user = userRepository.retrieveTouristOrLocalByToken(token);

        if (user == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(user.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        return "The code is verified correctly";
    }

    public String passwordResetStageThree(String token, String password) throws BadRequestException {
        System.out.println(token);
        User user = userRepository.retrieveTouristOrLocalByToken(token);

        if (user == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(user.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        user.setPassword(encoder.encode(password));
        user.setPassword_reset_token(null);
        user.setPassword_token_date(null);
        userRepository.save(user);

        try {
            String subject = "[WithinSG] Your Password was reset Successfully";
            String content = "<p>Dear " + user.getName() + ",</p>" +
                    "<p>Your password has been reset successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
        }

        return "Your password has been changed successfully";
    }

    public List<User> retrieveAllUser() {
        return userRepository.findAll();
    }
}
