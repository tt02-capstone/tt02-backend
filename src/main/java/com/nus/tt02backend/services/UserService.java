package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.exceptions.ToggleBlockException;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public void toggleBlock(Long userId) throws NotFoundException, ToggleBlockException {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user instanceof VendorStaff) {
                if (((VendorStaff) user).getIs_master_account() && !user.getIs_blocked()) { // master vendor staff
                    throw new ToggleBlockException("User access rights prevents him from being blocked!");
                }
            } else if (user instanceof InternalStaff && !user.getIs_blocked()) {
                throw new ToggleBlockException("Admin cannot be blocked!");
            }
            user.setIs_blocked(!user.getIs_blocked());
            userRepository.save(user);
        } else {
            throw new NotFoundException("User not found!");
        }
    }
}
