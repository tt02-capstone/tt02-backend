package com.nus.tt02backend.config;

import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.ApplicationStatusEnum;
import com.nus.tt02backend.models.enums.InternalRoleEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.models.enums.VendorEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitDataConfig implements CommandLineRunner {

    private final InternalStaffRepository internalStaffRepository;
    private final LocalRepository localRepository;
    private final TouristRepository touristRepository;
    private final VendorRepository vendorRepository;
    private final VendorStaffRepository vendorStaffRepository;
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

        if (localRepository.count() == 0) {
            Local local = new Local();
            local.setEmail("local@gmail.com");
            local.setName("Rowoon");
            local.setPassword(passwordEncoder.encode("password1!"));
            local.setUser_type(UserTypeEnum.LOCAL);
            local.setIs_blocked(false);
            local.setNric_num("S9911111A");
            local.setDate_of_birth(new Date());
            local.setWallet_balance(new BigDecimal(0));
            local.setCountry_code("+65");
            local.setMobile_num("98989898");
            localRepository.save(local);
        }

        if (touristRepository.count() == 0) {
            Tourist tourist = new Tourist();
            tourist.setEmail("tourist@gmail.com");
            tourist.setName("Cho Bo Ah");
            tourist.setPassword(passwordEncoder.encode("password1!"));
            tourist.setUser_type(UserTypeEnum.TOURIST);
            tourist.setIs_blocked(false);
            tourist.setPassport_num("A111111");
            tourist.setDate_of_birth(new Date());
            tourist.setCountry_code("+65");
            tourist.setMobile_num("9797979797");
            touristRepository.save(tourist);
        }

        if (vendorRepository.count() == 0) {
            Vendor vendor = new Vendor();
            vendor.setBusiness_name("Business Name");
            vendor.setPoc_name("Ha Joon");
            vendor.setPoc_position("Manager");
            vendor.setCountry_code("+65");
            vendor.setPoc_mobile_num("96969696");
            vendor.setWallet_balance(new BigDecimal(0));
            vendor.setApplication_status(ApplicationStatusEnum.APPROVED);
            vendor.setVendor_type(VendorEnum.ATTRACTION);
            vendor.setService_description("애정수를 믿으세요?");
            vendorRepository.save(vendor);

            VendorStaff vendorStaff = new VendorStaff();
            vendorStaff.setEmail("vendor@gmail.com");
            vendorStaff.setEmail_verified(true);
            vendorStaff.setName("Na Yeon");
            vendorStaff.setPassword(passwordEncoder.encode("password1!"));
            vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
            vendorStaff.setIs_blocked(false);
            vendorStaff.setPosition("Manager");
            vendorStaff.setIs_master_account(true);
            vendorStaff.setVendor(vendor);
            vendorStaffRepository.save(vendorStaff);
        }
    }
}
