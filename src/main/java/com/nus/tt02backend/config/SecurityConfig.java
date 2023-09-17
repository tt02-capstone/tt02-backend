package com.nus.tt02backend.config;

import com.nus.tt02backend.services.UserDetailsImpl;
import com.nus.tt02backend.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.nus.tt02backend.filters.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsImpl userService;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public AuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
      authProvider.setUserDetailsService(userService);
      authProvider.setPasswordEncoder(passwordEncoder);
      return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
      return config.getAuthenticationManager();
  }
  
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
    .csrf(csrf -> csrf 
      .disable()
    )
    .sessionManagement(session -> session
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    .authorizeHttpRequests(authorize -> authorize
      .requestMatchers(HttpMethod.POST,
              "/user/mobileLogin/**",
              "/user/webLogin/**",
              "/local/create",
              "/tourist/create",
              "/admin/createStaff",
              "/admin/staffLogin/**",
              "/admin/passwordResetStageOne/**",
              "/admin/passwordResetStageTwo/**",
              "/admin/passwordResetStageThree/**",
              "/vendorStaff/createVendorStaff",
              "/user/passwordResetStageOne/**",
              "/user/webPasswordResetStageOne/**",
              "/user/passwordResetStageTwo/**",
              "/user/webPasswordResetStageTwo/**",
              "/user/passwordResetStageThree/**",
              "/user/webPasswordResetStageThree/**").permitAll()
      .requestMatchers(HttpMethod.GET, "/api/v1/test/**").permitAll()
      .anyRequest().authenticated()
    )
    .authenticationProvider(authenticationProvider()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
