package com.nus.tt02backend.config;

import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import com.nus.tt02backend.services.AttractionService;
import com.nus.tt02backend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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
    private final AttractionRepository attractionRepository;
    private final TicketPerDayRepository ticketPerDayRepository;
    @Autowired
    PaymentService paymentService;

    @Autowired
    AttractionService attractionService;

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
            local.setEmail_verified(true);
            local.setMobile_num("98989898");

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "local@gmail.com");
            customer_parameters.put("name", "Rowoon");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            local.setStripe_account_id(stripe_account_id);

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
            tourist.setEmail_verified(true);
            tourist.setMobile_num("9797979797");

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "tourist@gmail.com");
            customer_parameters.put("name", "Cho Bo Ah");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            tourist.setStripe_account_id(stripe_account_id);

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

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "vendor@gmail.com");
            customer_parameters.put("name", "Business Name");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            vendor.setStripe_account_id(stripe_account_id);

            vendorRepository.save(vendor);

            VendorStaff vendorStaff = new VendorStaff();
            vendorStaff.setEmail("vendor@gmail.com");
            vendorStaff.setEmail_verified(true);
            vendorStaff.setName("Na Yeon"); //ewww
            vendorStaff.setPassword(passwordEncoder.encode("password1!"));
            vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
            vendorStaff.setIs_blocked(false);
            vendorStaff.setPosition("Manager");
            vendorStaff.setIs_master_account(true);
            vendorStaff.setVendor(vendor);
            vendorStaffRepository.save(vendorStaff);
            log.debug("created Vendor user - {}", vendorStaff);
        }

        if (attractionRepository.count() == 0) {
            Attraction attraction = new Attraction();
            attraction.setName("Mega Adventure Singapore");
            attraction.setDescription("Mega Adventure Park Singapore is located on the picturesque Sentosa Island, host to " +
                    "Singapore’s main attractions. The park operates world famous MegaZip flying fox, spanning 450m, flying at " +
                    "60 km/hour");
            attraction.setAddress("10A Siloso Bch Walk, 099008");
            attraction.setOpening_hours("11am - 6pm");
            attraction.setAge_group("Suitable for all ages");
            attraction.setContact_num("62353535");
            attraction.setIs_published(true);
            attraction.setSuggested_duration(4);
            attraction.setAvg_rating_tier(0.0);
            attraction.setAttraction_category(AttractionCategoryEnum.ENTERTAINMENT);
            attraction.setGeneric_location(GenericLocationEnum.SENTOSA);
            attraction.setAttraction_image_list(new ArrayList<>());
            attraction.getAttraction_image_list().add("http://tt02.s3-ap-southeast-1.amazonaws.com/attraction/init/mega1.jpeg");
            attraction.getAttraction_image_list().add("http://tt02.s3-ap-southeast-1.amazonaws.com/attraction/init/mega2.jpeg");

            Price childPrice = new Price();
            childPrice.setLocal_amount(new BigDecimal(30));
            childPrice.setTourist_amount(new BigDecimal(40));
            childPrice.setTicket_type(TicketEnum.CHILD);

            Price adultPrice = new Price();
            adultPrice.setLocal_amount(new BigDecimal(40));
            adultPrice.setTourist_amount(new BigDecimal(50));
            adultPrice.setTicket_type(TicketEnum.ADULT);

            List<Price> priceList = new ArrayList<>();
            priceList.add(childPrice);
            priceList.add(adultPrice);
            PriceTierEnum priceTier = attractionService.priceTierEstimation(priceList);

            attraction.setPrice_list(priceList);
            attraction.setEstimated_price_tier(priceTier); // set the pricing tier here

            TicketPerDay t1 = new TicketPerDay();
            t1.setTicket_date(LocalDate.parse("2023-10-13"));
            t1.setTicket_count(5);
            t1.setTicket_type(TicketEnum.ADULT);
            t1 = ticketPerDayRepository.save(t1);

            TicketPerDay t2 = new TicketPerDay();
            t2.setTicket_date(LocalDate.parse("2023-10-13"));
            t2.setTicket_count(5);
            t2.setTicket_type(TicketEnum.CHILD);
            t2 = ticketPerDayRepository.save(t2);

            TicketPerDay t3 = new TicketPerDay();
            t3.setTicket_date(LocalDate.parse("2023-10-14"));
            t3.setTicket_count(5);
            t3.setTicket_type(TicketEnum.ADULT);
            t3 = ticketPerDayRepository.save(t3);

            TicketPerDay t4 = new TicketPerDay();
            t4.setTicket_date(LocalDate.parse("2023-10-14"));
            t4.setTicket_count(5);
            t4.setTicket_type(TicketEnum.CHILD);
            t4 = ticketPerDayRepository.save(t4);

            attraction.setTicket_per_day_list(new ArrayList<>());
            attraction.getTicket_per_day_list().add(t1);
            attraction.getTicket_per_day_list().add(t2);
            attraction.getTicket_per_day_list().add(t3);
            attraction.getTicket_per_day_list().add(t4);

            attraction.setListing_type(ListingTypeEnum.ATTRACTION);

            attraction = attractionRepository.save(attraction);
            Vendor vendor = vendorRepository.findVendorByBusinessName("Business Name");

            List<Attraction> currentList = new ArrayList<>();
            currentList.add(attraction);
            vendor.setAttraction_list(currentList);
            vendorRepository.save(vendor);

            createSecondAttraction(currentList);
        }

    }

    public void createSecondAttraction(List<Attraction> currentList) {
        Attraction attraction = new Attraction();
        attraction.setName("Universal Studios Singapore");
        attraction.setDescription("Universal Studios Singapore is a theme park located within the Resorts World Sentosa " +
                "integrated resort at Sentosa in Singapore. It features 28 rides, shows, and attractions in seven themed " +
                "zones. It is one of the five Universal Studios theme parks around the world.");
        attraction.setAddress("8 Sentosa Gateway, Singapore 098269");
        attraction.setOpening_hours("11am - 6pm");
        attraction.setAge_group("Suitable for all ages");
        attraction.setContact_num("65778888");
        attraction.setIs_published(true);
        attraction.setSuggested_duration(5);
        attraction.setAvg_rating_tier(0.0);
        attraction.setAttraction_category(AttractionCategoryEnum.ADVENTURE);
        attraction.setGeneric_location(GenericLocationEnum.SENTOSA);
        attraction.setAttraction_image_list(new ArrayList<>());
        attraction.getAttraction_image_list().add("http://tt02.s3-ap-southeast-1.amazonaws.com/attraction/init/uss1.jpeg");
        attraction.getAttraction_image_list().add("http://tt02.s3-ap-southeast-1.amazonaws.com/attraction/init/uss2.jpeg");

        Price childPrice = new Price();
        childPrice.setLocal_amount(new BigDecimal(30));
        childPrice.setTourist_amount(new BigDecimal(40));
        childPrice.setTicket_type(TicketEnum.CHILD);

        Price adultPrice = new Price();
        adultPrice.setLocal_amount(new BigDecimal(40));
        adultPrice.setTourist_amount(new BigDecimal(50));
        adultPrice.setTicket_type(TicketEnum.ADULT);

        List<Price> priceList = new ArrayList<>();
        priceList.add(childPrice);
        priceList.add(adultPrice);
        PriceTierEnum priceTier = attractionService.priceTierEstimation(priceList);

        attraction.setPrice_list(priceList);
        attraction.setEstimated_price_tier(priceTier); // set the pricing tier here

        TicketPerDay t1 = new TicketPerDay();
        t1.setTicket_date(LocalDate.parse("2023-10-13"));
        t1.setTicket_count(5);
        t1.setTicket_type(TicketEnum.ADULT);
        t1 = ticketPerDayRepository.save(t1);

        TicketPerDay t2 = new TicketPerDay();
        t2.setTicket_date(LocalDate.parse("2023-10-13"));
        t2.setTicket_count(5);
        t2.setTicket_type(TicketEnum.CHILD);
        t2 = ticketPerDayRepository.save(t2);

        TicketPerDay t3 = new TicketPerDay();
        t3.setTicket_date(LocalDate.parse("2023-10-14"));
        t3.setTicket_count(5);
        t3.setTicket_type(TicketEnum.ADULT);
        t3 = ticketPerDayRepository.save(t3);

        TicketPerDay t4 = new TicketPerDay();
        t4.setTicket_date(LocalDate.parse("2023-10-14"));
        t4.setTicket_count(5);
        t4.setTicket_type(TicketEnum.CHILD);
        t4 = ticketPerDayRepository.save(t4);

        attraction.setTicket_per_day_list(new ArrayList<>());
        attraction.getTicket_per_day_list().add(t1);
        attraction.getTicket_per_day_list().add(t2);
        attraction.getTicket_per_day_list().add(t3);
        attraction.getTicket_per_day_list().add(t4);

        attraction.setListing_type(ListingTypeEnum.ATTRACTION);

        attractionRepository.save(attraction);
        Vendor vendor = vendorRepository.findVendorByBusinessName("Business Name");
        currentList.add(attraction); // add on to the previous list
        vendor.setAttraction_list(currentList);
        vendorRepository.save(vendor);
    }
}
