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

    public User userMobileLogin(String email, String password) throws NotFoundException, BadRequestException {
        User checkUser = userRepository.retrieveTouristOrLocalByEmail(email);
        if (checkUser == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkUser.getPassword()) && !checkUser.getIs_blocked()) {
            //can check and initialise here foreign keys here
            if (checkUser instanceof Tourist) {
                Tourist tourist = (Tourist) checkUser;
                tourist.setBooking_list(null);
                tourist.setPost_list(null);
                tourist.setComment_list(null);
                tourist.setCart_list(null);
                tourist.setSupport_ticket_list(null);

//                List<SupportTicket> supportTicketList = tourist.getSupport_ticket_list();
//                for (SupportTicket s : supportTicketList) {
//                    List<Reply> replyList = s.getReply_list();
//                    for (Reply r : replyList) {
//                        if (r.getLocal_user() != null) {
//                            r.getLocal_user().setSupport_ticket_list(null);
//                        } else if (r.getTourist_user() != null) {
//                            r.getTourist_user().setSupport_ticket_list(null);
//                        } else if (r.getVendor_staff_user() != null) {
//                            r.getVendor_staff_user().setIncoming_support_ticket_list(null);
//                            r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
//                            r.getVendor_staff_user().getVendor().setVendor_staff_list(null);
//                        } else if (r.getInternal_staff_user() != null) {
//                            r.getInternal_staff_user().setSupport_ticket_list(null);
//                        }
//                    }
//                }
//
//                tourist.setSupport_ticket_list(supportTicketList);

                return tourist;

            } else if (checkUser instanceof Local) {
                Local local = (Local) checkUser;
                local.setBooking_list(null);
                local.setPost_list(null);
                local.setComment_list(null);
                local.setCart_list(null);
                local.setSupport_ticket_list(null);

//                List<SupportTicket> supportTicketList = local.getSupport_ticket_list();
//                for (SupportTicket s : supportTicketList) {
//                    List<Reply> replyList = s.getReply_list();
//                    for (Reply r : replyList) {
//                        if (r.getLocal_user() != null) {
//                            r.getLocal_user().setSupport_ticket_list(null);
//                        } else if (r.getTourist_user() != null) {
//                            r.getTourist_user().setSupport_ticket_list(null);
//                        } else if (r.getVendor_staff_user() != null) {
//                            r.getVendor_staff_user().setIncoming_support_ticket_list(null);
//                            r.getVendor_staff_user().setOutgoing_support_ticket_list(null);
//                            r.getVendor_staff_user().getVendor().setVendor_staff_list(null);
//                        } else if (r.getInternal_staff_user() != null) {
//                            r.getInternal_staff_user().setSupport_ticket_list(null);
//                        }
//                    }
//                }
//                local.setSupport_ticket_list(supportTicketList);

                return local;

            } else {
                throw new NotFoundException("User is not a tourist or local!");
            }

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
                    vendorStaff.getVendor().setVendor_staff_list(null);
                    vendorStaff.setIncoming_support_ticket_list(null);
                    vendorStaff.setOutgoing_support_ticket_list(null);
                    vendorStaff.setComment_list(null);
                    vendorStaff.setPost_list(null);

                    return vendorStaff;
                } else if (vendorStaff.getVendor().getApplication_status() == ApplicationStatusEnum.REJECTED) {
                    throw new BadRequestException("Your application was rejected, contact us for more information.");
                } else if (!checkUser.getEmail_verified()) {
                    String emailVerificationLink = "http://localhost:3001/verifyemail?token=" + checkUser.getEmail_verification_token();
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
            } else {
                Local local = (Local) checkUser;
                local.setCard_list(null);
                local.setWithdrawal_list(null);
                local.setComment_list(null);
                local.setPost_list(null);
                local.setBadge_list(null);
                local.setCart_list(null);
                local.setBooking_list(null);
                local.setTour_type_list(null);
                local.setAttraction_list(null);
                local.setAccommodation_list(null);
                local.setRestaurant_list(null);
                local.setTelecom_list(null);
                local.setDeals_list(null);
                local.setSupport_ticket_list(null);

                return local;
            }
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

    public String verifyEmail(String token) throws BadRequestException {
        System.out.println(token);
        User user = userRepository.retrieveTouristByEmailVerificationToken(token);

        if (user == null) {
            throw new BadRequestException("Invalid token");
        }

        if (Duration.between(user.getToken_date(), LocalDateTime.now()).toMinutes() > 60) {
            throw new BadRequestException("Your token has expired, please request for a new password reset link");
        }

        user.setEmail_verification_token(null);
        user.setEmail_verified(true);
        user.setToken_date(null);
        userRepository.save(user);

        try {
            String subject = "[WithinSG] Email Verified Successfully";
            String content = "<p>Dear " + user.getName() + ",</p>" +
                    "<p>Your email has been verified successfully." +
                    "<p>If you did not perform this action, please let us know immediately by replying to this email</p>" +
                    "<p>Kind Regards,<br> WithinSG</p>";
            sendEmail(user.getEmail(), subject, content);
        } catch (MessagingException ex) {
            throw new BadRequestException("We encountered a technical error while sending the successful email verification email");
        }

        return "Your email has been verified successfully";
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
                    "<p>Please enter your verification code into the WithinSG application: </p>" +
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

    public String webPasswordResetStageOne(String email) throws BadRequestException {
        UUID uuid = UUID.randomUUID();
        long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
        String passwordResetOTP =  String.format("%04d", otpValue);

        User user = userRepository.retrieveVendorStaffOrLocalByEmail(email);

        if (user == null) {
            throw new BadRequestException("There is no account associated with this email address");
        }

        user.setPassword_reset_token(passwordResetOTP);
        user.setPassword_token_date(LocalDateTime.now());
        userRepository.save(user);

        String passwordResetLink = "http://localhost:3001/passwordreset";
        try {
            String subject = "[WithinSG] Your Password Reset Instructions";
            String content = "<p>Dear " + user.getName() + ",</p>" +
                    "<p>A request was received to reset the password for your account." +
                    "<p>Please enter your verification code <b>" + user.getPassword_reset_token() + "</b> into the WithinSG platform: </p>" +
                    "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                    "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">Reset Password</button></a>" +
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

    public String webPasswordResetStageTwo(String email, String token) throws BadRequestException {
        User user = userRepository.retrieveVendorStaffOrLocalByEmail(email);

        if (user == null) {
            throw new BadRequestException("Invalid email");
        }

        if (!user.getPassword_reset_token().equals(token)) {
            throw new BadRequestException("Invalid OTP");
        }

        if (Duration.between(user.getPassword_token_date(), LocalDateTime.now()).toMinutes() > 60) {
            UUID uuid = UUID.randomUUID();
            long otpValue = Math.abs(uuid.getLeastSignificantBits() % 10000); // Get the last 4 digits
            String passwordResetOTP =  String.format("%04d", otpValue);

            user.setPassword_reset_token(passwordResetOTP);
            user.setPassword_token_date(LocalDateTime.now());
            userRepository.save(user);

            String passwordResetLink = "http://localhost:3001/passwordreset";
            try {
                String subject = "[WithinSG] Your Password Reset Instructions";
                String content = "<p>Dear " + user.getName() + ",</p>" +
                        "<p>A request was received to reset the password for your account." +
                        "<p>Please enter your verification code <b>" + user.getPassword_reset_token() + "</b> into the WithinSG platform: </p>" +
                        "<a href=\"" + passwordResetLink +"\" target=\"_blank\">" +
                        "<button style=\"background-color: #F6BE00; color: #000; padding: 10px 20px; border: none; cursor: pointer;\">Reset Password</button></a>" +
                        "<p>Note that the code will expire after 60 minutes.</p>" +
                        "<p>If you did not initiate this request, please let us know immediately by replying to this email</p>" +
                        "<p>Kind Regards,<br> WithinSG</p>";
                sendEmail(user.getEmail(), subject, content);
            } catch (MessagingException ex) {
                throw new BadRequestException("We encountered a technical error while sending the signup confirmation email");
            }

            throw new BadRequestException("Your OTP has expired, a new OTP has been sent to your email");
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

    public void editPassword(Long userId, String oldPassword, String newPassword) throws BadRequestException {
        try {
            System.out.println(userId + ", " + oldPassword + ", " + newPassword);
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new BadRequestException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, user.getPassword())) {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);

                } else {
                    throw new BadRequestException("Incorrect old password!");
                }

            } else {
                throw new BadRequestException("User not found!");
            }
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public User uploadNewProfilePic(Long userId, String img) throws NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setProfile_pic(img);
            userRepository.save(user);

            if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                tourist.setBooking_list(null);
                tourist.setPost_list(null);
                tourist.setComment_list(null);
                return tourist;

            } else if (user instanceof Local) {
                Local local = (Local) user;
                local.setBooking_list(null);
                local.setPost_list(null);
                local.setComment_list(null);
                return local;

            } else if (user instanceof VendorStaff) {
                VendorStaff vendorStaff = (VendorStaff) user;
                vendorStaff.getVendor().setVendor_staff_list(null);
                return vendorStaff;

            } else {
                InternalStaff internalStaff = (InternalStaff) user;
                return internalStaff;
            }
        } else {
            throw new NotFoundException("User not found!");
        }
    }

    // admin blocking, cannot use on vendor portal
    public void toggleBlock(Long userId) throws NotFoundException, ToggleBlockException {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user instanceof InternalStaff && !user.getIs_blocked()) {
                throw new ToggleBlockException("Admin cannot be blocked!");
            }

            user.setIs_blocked(!user.getIs_blocked());
            userRepository.save(user);

        } else {
            throw new NotFoundException("User not found!");
        }
    }

    public String webPasswordResetStageThree(String email, String password) throws BadRequestException {
        User user = userRepository.retrieveVendorStaffOrLocalByEmail(email);

        if (user == null) {
            throw new BadRequestException("Invalid email");
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

    public User viewUserProfile(Long userId) throws NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user instanceof VendorStaff) {
                VendorStaff vendorStaff = (VendorStaff) user;
                vendorStaff.getVendor().setVendor_staff_list(null);
                return vendorStaff;

            }  else if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                tourist.setBooking_list(null);
                tourist.setPost_list(null);
                tourist.setComment_list(null);
                return tourist;

            } else if (user instanceof Local) {
                Local local = (Local) user;
                local.setBooking_list(null);
                local.setPost_list(null);
                local.setComment_list(null);
                return local;
            }

            return user; // internal staff
        } else {
            throw new NotFoundException("User not found!");
        }
    }

    public List<User> retrieveAllUser() {
        return userRepository.findAll();
    }
}
