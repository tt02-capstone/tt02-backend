package com.nus.tt02backend.config;

import com.nus.tt02backend.models.InternalStaff;
import com.nus.tt02backend.models.User;
import com.nus.tt02backend.models.enums.InternalRoleEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.InternalStaffRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitDataConfig implements CommandLineRunner {

    private final InternalStaffRepository internalStaffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        if (internalStaffRepository.count() == 0) {

            InternalStaff staff = InternalStaff.builder()
                    .email("admin@gmail.com")
                    .name("admin")
                    .password(passwordEncoder.encode("password1!"))
                    .user_type(UserTypeEnum.INTERNAL_STAFF)
                    .is_blocked(false)
                    .role(InternalRoleEnum.ADMIN)
                    .staff_num(48323233L)
                    .build();

            internalStaffRepository.save(staff);
            log.debug("created ADMIN user - {}", staff);
        }
    }

}
