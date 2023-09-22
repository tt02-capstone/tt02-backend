package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.dto.JwtRefreshResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import com.nus.tt02backend.repositories.LocalRepository;
import com.nus.tt02backend.repositories.TouristRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDetailsImpl userDetailsImpl;
    private final UserService userService;
    private final VendorStaffService vendorStaffService;
    private final VendorStaffRepository vendorStaffRepository;
    private final InternalStaffService internalStaffService;
    private final InternalStaffRepository internalStaffRepository;
    private final LocalService localService;
    private final LocalRepository localRepository;
    private final TouristService touristService;
    private final TouristRepository touristRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public JwtRefreshResponse refreshToken(HttpServletRequest request) throws BadRequestException, NotFoundException {
        try {
            DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");
            Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
            String userEmail = expectedMap.get("sub").toString();
            UserDetails userDetails = userDetailsImpl.loadUserByUsername(userEmail);
            if (!userEmail.equals(userDetails.getUsername())) {
                throw new BadCredentialsException("Mismatch is credentials");
            }
            String newToken = jwtService.doGenerateRefreshToken(userDetails);
            return new JwtRefreshResponse(newToken);
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }

    public JwtAuthenticationResponse staffLogin(String email, String password) throws BadRequestException, NotFoundException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            InternalStaff internalStaff = internalStaffService.staffLogin(email, password);
            UserDetails details = userDetailsImpl.loadUserByUsername(email);
            String jwt = jwtService.generateToken(details);
            return new JwtAuthenticationResponse(jwt, internalStaff);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse userMobileLogin(String email, String password) throws BadRequestException, NotFoundException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            User user = userService.userMobileLogin(email, password);
            UserDetails ud = userDetailsImpl.loadUserByUsername(email);
            String jwt = jwtService.generateToken(ud);
            user.setPassword(null);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse userWebLogin(String email, String password) throws BadRequestException, NotFoundException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            User user = userService.userWebLogin(email, password);
            UserDetails ud = userDetailsImpl.loadUserByUsername(email);
            String jwt = jwtService.generateToken(ud);
            user.setPassword(null);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse editInternalStaffProfile(InternalStaff internalStaffToEdit) throws BadRequestException {
        try {
            Optional<InternalStaff> internalStaffOptional = internalStaffRepository.findById(internalStaffToEdit.getUser_id());
            if (internalStaffOptional.isPresent()) {
                InternalStaff internalStaff = internalStaffOptional.get();
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(internalStaff.getEmail(), internalStaffToEdit.getPassword()));
                InternalStaff edittedInternalStaff = internalStaffService.editProfile(internalStaffToEdit);
                UserDetails ud = userDetailsImpl.loadUserByUsername(edittedInternalStaff.getEmail());
                String jwt = jwtService.generateToken(ud);
                edittedInternalStaff.setPassword(null);
                return new JwtAuthenticationResponse(jwt, edittedInternalStaff);

            } else {
                throw new BadRequestException("Admin not found!");
            }
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse editVendorStaffProfile(VendorStaff userToEdit) throws BadRequestException {
        try {
            Optional<VendorStaff> userOptional = vendorStaffRepository.findById(userToEdit.getUser_id());
            if (userOptional.isPresent()) {
                VendorStaff user = userOptional.get();
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), userToEdit.getPassword()));
                VendorStaff edittedUser = vendorStaffService.editVendorStaffProfile(userToEdit);
                UserDetails ud = userDetailsImpl.loadUserByUsername(edittedUser.getEmail());
                String jwt = jwtService.generateToken(ud);
                edittedUser.setPassword(null);
                return new JwtAuthenticationResponse(jwt, edittedUser);

            } else {
                throw new BadRequestException("Vendor staff not found!");
            }
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse editLocalProfile(Local userToEdit) throws BadRequestException {
        try {
            Optional<Local> userOptional = localRepository.findById(userToEdit.getUser_id());
            if (userOptional.isPresent()) {
                Local user = userOptional.get();
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), userToEdit.getPassword()));
                Local edittedUser = localService.editLocalProfile(userToEdit);
                UserDetails ud = userDetailsImpl.loadUserByUsername(edittedUser.getEmail());
                String jwt = jwtService.generateToken(ud);
                edittedUser.setPassword(null);
                return new JwtAuthenticationResponse(jwt, edittedUser);

            } else {
                throw new BadRequestException("Local not found!");
            }
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse editTouristProfile(Tourist userToEdit) throws BadRequestException {
        try {
            Optional<Tourist> userOptional = touristRepository.findById(userToEdit.getUser_id());
            if (userOptional.isPresent()) {
                Tourist user = userOptional.get();
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getEmail(), userToEdit.getPassword()));
                Tourist edittedUser = touristService.editTouristProfile(userToEdit);
                UserDetails ud = userDetailsImpl.loadUserByUsername(edittedUser.getEmail());
                String jwt = jwtService.generateToken(ud);
                edittedUser.setPassword(null);
                return new JwtAuthenticationResponse(jwt, edittedUser);

            } else {
                throw new BadRequestException("Local not found!");
            }
        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }
}
