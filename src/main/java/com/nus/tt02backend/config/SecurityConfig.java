package com.nus.tt02backend.config;

import com.nus.tt02backend.services.UserDetailsImpl;
import com.nus.tt02backend.services.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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

//  @Bean("corsConfigurationSource")
//  CorsConfigurationSource corsConfigurationSource() {
//      CorsConfiguration configuration = new CorsConfiguration();
//      configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
//      configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
//      configuration.setAllowedHeaders(Arrays.asList("*"));
//      configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT"));
//      configuration.setAllowCredentials(true);
//      configuration.setExposedHeaders(List.of("Authorization"));
//
//      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//      source.registerCorsConfiguration("/**", configuration);
//      return source;
//  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
//    .cors(Customizer.withDefaults())
    .csrf(csrf -> csrf
      .disable()
    )
    .sessionManagement(session -> session
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    .authorizeHttpRequests(authorize -> authorize
        .requestMatchers(HttpMethod.POST,
              "/local/create",
              "/tourist/create",
              "/admin/createStaff",
              "/admin/staffLogin/**",
              "/admin/passwordResetStageOne/**",
              "/admin/passwordResetStageTwo/**",
              "/admin/passwordResetStageThree/**",
              "/vendor/createVendor",
              "/vendorStaff/createVendorStaff",
              "/category/**", // to remove
              "/categoryItem/**", // to remove
              "/post/**", // to remove
              "/comment/**", // to remove
              "/supportTicket/**", // to remove
              "/reply/**", // to remove
              "/report/**", // to remove
              "/user/**").permitAll()
        .requestMatchers(HttpMethod.GET,
              "/vendorStaff/verifyEmail/**",
              "/category/**", // to remove
              "/categoryItem/**", // to remove
              "/post/**", // to remove
              "/comment/**", // to remove
              "/supportTicket/**", // to remove
              "/reply/**", // to remove
              "/booking/**", // to remove
              "/report/**", // to remove
              "/user/**").permitAll()
        .requestMatchers(HttpMethod.PUT,
              "/category/**", // to remove
              "/categoryItem/**", // to remove
              "/post/**", // to remove
              "/comment/**", // to remove
              "/supportTicket/**", // to remove
              "/reply/**", // to remove
              "/report/**", // to remove
              "/user/**").permitAll()
        .requestMatchers(HttpMethod.DELETE,
                "/supportTicket/**" // to remove
                ).permitAll()
        .requestMatchers(HttpMethod.GET,
              "/category/**", // to remove
              "/categoryItem/**", // to remove
              "/post/**", // to remove
              "/comment/**", // to remove
              "/vendorStaff/verifyEmail/**",
              "/user/**").permitAll()
        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
      .anyRequest().authenticated()
    )
    .authenticationProvider(authenticationProvider()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
