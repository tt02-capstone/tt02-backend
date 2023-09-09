package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Local;
import com.nus.tt02backend.models.Tourist;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;

    PasswordEncoder encoder = new BCryptPasswordEncoder();

    // only for tourists and locals
    public User userLogin(String email, String password) throws NotFoundException, BadRequestException {

        List<Tourist> tourists = touristRepository.findAll();

        for (Tourist tourist : tourists) {
            if (tourist.getEmail().equals(email)) {
                if (encoder.matches(password, tourist.getPassword())) {
                    return tourist;
                } else {
                    throw new BadRequestException("Incorrect password!");
                }
            }
        }

        List<Local> locals = localRepository.findAll();
        for (Local local : locals) {
            if (local.getEmail().equals(email)) {
                if (encoder.matches(password, local.getPassword())) {
                    return local;
                } else {
                    throw new BadRequestException("Incorrect password!");
                }
            }
        }

        throw new NotFoundException("Account not found!");
    }

    // only for tourists and locals
    public User getUserProfile(Long id) throws IllegalArgumentException, UserNotFoundException {
        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(null);
                return user;

            } else {
                throw new UserNotFoundException("User not found!");
            }

        } catch(Exception ex) {
            throw new UserNotFoundException(ex.getMessage());
        }
    }

    public User editUserProfile(User userToEdit) throws EditUserException {
        try {

            Optional<User> userOptional = userRepository.findById(userToEdit.getUser_id());

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (!user.getEmail().equals(userToEdit.getEmail())) { // user wants to change email
                    Long existingId = userRepository.getUserIdByEmail(userToEdit.getEmail());
                    if (existingId != null) { // but there is an existing email
                        throw new EditUserException("Email currently in use. Please use a different email!");
                    }
                }

                user.setEmail(user.getEmail());
                user.setName(user.getName());
                userRepository.save(user);
                user.setPassword(null);
                return user;

            } else {
                throw new EditUserException("User not found!");
            }
        } catch (Exception ex) {
            throw new EditUserException(ex.getMessage());
        }
    }

    public void editUserPassword(Long id, String oldPassword, String newPassword) throws EditPasswordException {
        try {
            Optional<User> userOptional = userRepository.findById(id);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (oldPassword.equals(newPassword)) {
                    throw new EditPasswordException("New password must be different from old password!");

                } else if (encoder.matches(oldPassword, user.getPassword())) {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);

                } else {
                    throw new EditPasswordException("Incorrect old password!");
                }

            } else {
                throw new EditUserException("User not found!");
            }
        } catch (Exception ex) {
            throw new EditPasswordException(ex.getMessage());
        }
    }
}
