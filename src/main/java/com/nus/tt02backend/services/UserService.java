package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    VendorStaffRepository vendorStaffRepository;
    @Autowired
    LocalRepository localRepository;

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
//    public User getUserProfile(Long id) throws IllegalArgumentException, UserNotFoundException {
//        try {
//            Optional<User> userOptional = userRepository.findById(id);
//
//            if (userOptional.isPresent()) {
//                User user = userOptional.get();
//                user.setPassword(null);
//
//                if (user instanceof VendorStaff) {
//                    VendorStaff vendorStaff = (VendorStaff) user;
//                    vendorStaff.getVendor().setVendor_staff_list(null);
//                    vendorStaff.setComment_list(null);
//                    return vendorStaff;
//                } else if (user instanceof Tourist) {
//                    Tourist tourist = (Tourist) user;
//                    tourist.setBooking_list(null);
//                    tourist.setComment_list(null);
//                } else if (user instanceof Local) {
//                    Local local = (Local) user;
//                    local.setBooking_list(null);
//                    local.setComment_list(null);
//                } else if (user instanceof InternalStaff) {
//                    InternalStaff internalStaff = (InternalStaff) user;
//                    internalStaff.setComment_list(null);
//                }
//
//                return user;
//
//            } else {
//                throw new UserNotFoundException("User not found!");
//            }
//
//        } catch(Exception ex) {
//            throw new UserNotFoundException(ex.getMessage());
//        }
//    }
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
}
