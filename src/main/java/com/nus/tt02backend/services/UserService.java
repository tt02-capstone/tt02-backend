package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
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
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JavaMailSender javaMailSender;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    public User userLogin(String email, String password) throws NotFoundException, BadRequestException {
        User checkUser = userRepository.retrieveUserByEmail(email);

        if (checkUser == null) {
            throw new NotFoundException("There is no account associated with this email address");
        }

        if (encoder.matches(password, checkUser.getPassword())
                && !checkUser.getIs_blocked()) {
//            //still need to add more
//            checkLocal.setComment_list(new ArrayList<>());
//            checkLocal.setPost_list(null);
//            checkLocal.setBadge_list(null);
//            checkLocal.setSupport_ticket_list(null);
//            checkLocal.setAttraction_list(null);
//            checkLocal.setPost_list(null);
//            checkLocal.setAccommodation_list(null);
//            checkLocal.setCard_list(null);
//            checkLocal.setCart_list(null);
//            checkLocal.setRestaurant_list(null);
//            checkLocal.setTelecom_list(null);
//            checkLocal.setTour_type_list(null);
//            checkLocal.setItinerary(null);
            return checkUser;

        } else if (checkUser.getIs_blocked()) {
            throw new BadRequestException("Your account is disabled, please contact our help desk");
        } else {
            throw new BadRequestException("Incorrect password");
        }
    }

    public List<User> retrieveAllLocal() {
        return userRepository.findAll();
    }
}
