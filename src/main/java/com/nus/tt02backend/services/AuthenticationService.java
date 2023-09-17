package com.nus.tt02backend.services;

import com.nus.tt02backend.dto.JwtAuthenticationResponse;
import com.nus.tt02backend.dto.SignInRequest;
import com.nus.tt02backend.dto.SignUpRequest;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.ErrorResponse;
import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserDetailsImpl userDetailsImpl;
    private final UserService userService;
    private final InternalStaffService internalStaffService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse staffLogin(String email, String password) throws BadRequestException, NotFoundException {
        try {
            System.out.println("In Staff Login");
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            System.out.println("Authentication manager " + authentication);
            InternalStaff internalStaff = internalStaffService.staffLogin(email, password);
            UserDetails details = userDetailsImpl.loadUserByUsername(email);
            System.out.println(details);
            System.out.println(details.getAuthorities());
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
            System.out.println("Authentication manager " + authentication);
            User user = userService.userMobileLogin(email, password);
            UserDetails ud = userDetailsImpl.loadUserByUsername(email);
            System.out.println("Get auth" + ud.getAuthorities());
            String jwt = jwtService.generateToken(ud);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    public JwtAuthenticationResponse userWebLogin(String email, String password) throws BadRequestException, NotFoundException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            System.out.println("Authentication manager " + authentication);
            User user = userService.userWebLogin(email, password);
            UserDetails ud = userDetailsImpl.loadUserByUsername(email);
            System.out.println("Get auth" + ud.getAuthorities());
            String jwt = jwtService.generateToken(ud);
            return new JwtAuthenticationResponse(jwt, user);

        } catch (Exception e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

}
