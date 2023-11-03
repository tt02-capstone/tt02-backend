package com.nus.tt02backend.services;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.repositories.UserRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InternalStaffRepository internalStaffRepository;


    @Autowired
    private VendorStaffRepository vendorStaffRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user =  userRepository.retrieveUserEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<String> roles = new ArrayList<>();
        roles.add(user.getUser_type().name());

        if (user.getUser_type() == UserTypeEnum.INTERNAL_STAFF){
            InternalStaff is = internalStaffRepository.retrieveInternalStaffByEmail(email);
            roles.add(is.getRole().name());
        } else if (user.getUser_type() == UserTypeEnum.VENDOR_STAFF) {
            VendorStaff vs = vendorStaffRepository.retrieveVendorStaffByEmail(email);
            if (vs.getIs_master_account()) {
                roles.add("VENDOR_ADMIN");
            }
        }

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(roles.toArray(new String[0]))
                        .build();

        return userDetails;
    }
}
