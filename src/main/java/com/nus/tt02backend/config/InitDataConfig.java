package com.nus.tt02backend.config;

import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import com.nus.tt02backend.services.AttractionService;
import com.nus.tt02backend.services.PaymentService;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Token;
import com.stripe.param.PaymentMethodCreateParams;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.*;
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
    private final AccommodationRepository accommodationRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final DealRepository dealRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryItemRepository categoryItemRepository;
    private final TelecomRepository telecomRepository;
    private final TourTypeRepository tourTypeRepository;
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final ReplyRepository replyRepository;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AttractionService attractionService;

    @Override
    public void run(String... args) throws Exception {

        if (internalStaffRepository.count() == 0) {
            InternalStaff staff = (InternalStaff) InternalStaff.builder()
                    .email("admin@gmail.com")
                    .name("Piyush Gupta")
                    .password(passwordEncoder.encode("password1!"))
                    .user_type(UserTypeEnum.INTERNAL_STAFF)
                    .is_blocked(false)
                    .role(InternalRoleEnum.ADMIN)
                    .staff_num(48323233L)
                    .profile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg")
                    .build();
            staff.setBadge_list(new ArrayList<>());
            internalStaffRepository.save(staff);
            log.debug("created ADMIN user - {}", staff);
        }

        Local local = new Local();
        if (localRepository.count() == 0) {
            local.setEmail("local@gmail.com");
            local.setName("Alice Tan");
            local.setPassword(passwordEncoder.encode("password1!"));
            local.setUser_type(UserTypeEnum.LOCAL);
            local.setIs_blocked(false);
            local.setNric_num("S9914132A");
            local.setDate_of_birth(new Date());
            local.setWallet_balance(new BigDecimal(0));
            local.setCountry_code("+65");
            local.setEmail_verified(true);
            local.setMobile_num("98989898");
            local.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
            local.setTour_type_list(new ArrayList<>());
            local.setBadge_list(new ArrayList<>());

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "local@gmail.com");
            customer_parameters.put("name", "Alice Tan");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            local.setStripe_account_id(stripe_account_id);

            local = localRepository.save(local);

            // init the ccd card here for the local account
            Map<String, Object> card = new HashMap<>();
            card.put("token", "tok_visa");

            Map<String, Object> params = new HashMap<>();
            params.put("type", "card");
            params.put("card", card);

            PaymentMethod paymentMethod = PaymentMethod.create(params);

            paymentService.addPaymentMethod("LOCAL", "local@gmail.com", paymentMethod.getId());
        }

        if (touristRepository.count() == 0) {
            Tourist tourist = new Tourist();
            tourist.setEmail("tourist@gmail.com");
            tourist.setName("Rowoon");
            tourist.setPassword(passwordEncoder.encode("password1!"));
            tourist.setUser_type(UserTypeEnum.TOURIST);
            tourist.setIs_blocked(false);
            tourist.setPassport_num("A152335");
            tourist.setDate_of_birth(new Date());
            tourist.setCountry_code("+82");
            tourist.setEmail_verified(true);
            tourist.setMobile_num("01037596775");
            tourist.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
            tourist.setBadge_list(new ArrayList<>());

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "tourist@gmail.com");
            customer_parameters.put("name", "Rowoon");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            tourist.setStripe_account_id(stripe_account_id);

            touristRepository.save(tourist);

            // init the ccd card here for the tourist account
            Map<String, Object> card = new HashMap<>();
            card.put("token", "tok_visa");

            Map<String, Object> params = new HashMap<>();
            params.put("type", "card");
            params.put("card", card);

            PaymentMethod paymentMethod = PaymentMethod.create(params);

            paymentService.addPaymentMethod("TOURIST", "tourist@gmail.com", paymentMethod.getId());
        }

        Vendor vendor1 = new Vendor();
        Vendor vendor2 = new Vendor();
        Vendor vendor3 = new Vendor();
        Vendor vendor4 = new Vendor();
        Vendor vendor5 = new Vendor();
        Vendor vendor6 = new Vendor();

        if (vendorRepository.count() == 0) {
            vendor1.setBusiness_name("Sentosa Leisure Management");
            vendor1.setPoc_name("Bob Tan Beng Hai");
            vendor1.setPoc_position("Manager");
            vendor1.setCountry_code("+65");
            vendor1.setPoc_mobile_num("96969696");
            vendor1.setWallet_balance(new BigDecimal(0));
            vendor1.setApplication_status(ApplicationStatusEnum.APPROVED);
            vendor1.setVendor_type(VendorEnum.ATTRACTION);
            vendor1.setService_description("Sentosa, a place where children dreams come true!");

            Map<String, Object> customer_parameters = new HashMap<>();
            customer_parameters.put("email", "attraction@gmail.com");
            customer_parameters.put("name", "Sentosa Leisure Management");
            String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
            vendor1.setStripe_account_id(stripe_account_id);

            vendor1 = vendorRepository.save(vendor1);

            VendorStaff vendorStaff = new VendorStaff();
            vendorStaff.setEmail("attraction@gmail.com");
            vendorStaff.setEmail_verified(true);
            vendorStaff.setName("Bob Tan Beng Hai");
            vendorStaff.setPassword(passwordEncoder.encode("password1!"));
            vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
            vendorStaff.setIs_blocked(false);
            vendorStaff.setPosition("Manager");
            vendorStaff.setIs_master_account(true);
            vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
            vendorStaff.setVendor(vendor1);
            vendorStaff.setBadge_list(new ArrayList<>());
            vendorStaffRepository.save(vendorStaff);
            log.debug("created Vendor user - {}", vendorStaff);

            vendor2 = setUpVendor2(vendor2); // telecom - M1
            vendor3 = setUpVendor3(vendor3); // accommodation
            vendor4 = setUpVendor4(vendor4); // restaurant - The Kitchen Table
            vendor5 = setUpVendor5(vendor5); // restaurant - 8 Noodles
            vendor6 = setUpVendor6(vendor6); // telecom - Singtel
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
            attraction.setTour_type_list(new ArrayList<>());

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
            t1.setTicket_date(LocalDate.parse("2023-10-27"));
            t1.setTicket_count(5);
            t1.setTicket_type(TicketEnum.ADULT);
            t1 = ticketPerDayRepository.save(t1);

            TicketPerDay t2 = new TicketPerDay();
            t2.setTicket_date(LocalDate.parse("2023-10-27"));
            t2.setTicket_count(5);
            t2.setTicket_type(TicketEnum.CHILD);
            t2 = ticketPerDayRepository.save(t2);

            TicketPerDay t3 = new TicketPerDay();
            t3.setTicket_date(LocalDate.parse("2023-10-27"));
            t3.setTicket_count(5);
            t3.setTicket_type(TicketEnum.ADULT);
            t3 = ticketPerDayRepository.save(t3);

            TicketPerDay t4 = new TicketPerDay();
            t4.setTicket_date(LocalDate.parse("2023-10-27"));
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

            List<Attraction> currentList = new ArrayList<>();
            currentList.add(attraction);
            vendor1.setAttraction_list(currentList);
            vendorRepository.save(vendor1);

            createSecondAttraction(currentList);
        }

        if (dealRepository.count() == 0) {
            Deal d1 = new Deal();
            d1.setStart_datetime(LocalDateTime.parse("2023-10-12T16:00:00"));
            d1.setEnd_datetime(LocalDateTime.parse("2024-10-13T16:00:00"));
            d1.setDiscount_percent(10);
            d1.setDeal_type(DealCategoryEnum.BLACK_FRIDAY);
            d1.setPromo_code("TOURING");
            d1.setIs_govt_voucher(false);
            d1.setIs_published(true);
            List<String> imgList = new ArrayList<>();
            imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/static/deals/deals_backfriday.jpeg");
            d1.setDeal_image_list(imgList);

            d1 = dealRepository.save(d1);
            List<Deal> dList = new ArrayList<>();
            dList.add(d1);
            vendor1.setDeals_list(dList);
            vendorRepository.save(vendor1);

            Deal d2 = new Deal();
            d2.setStart_datetime(LocalDateTime.parse("2023-10-12T16:00:00"));
            d2.setEnd_datetime(LocalDateTime.parse("2024-10-13T16:00:00"));
            d2.setDiscount_percent(20);
            d2.setDeal_type(DealCategoryEnum.GOVERNMENT);
            d2.setPromo_code("WELCOME");
            d2.setIs_govt_voucher(true);
            d2.setIs_published(true);
            List<String> imgList2 = new ArrayList<>();
            imgList2.add("https://tt02.s3.ap-southeast-1.amazonaws.com/static/deals/deals_gov.png");
            d2.setDeal_image_list(imgList2);

            d2 = dealRepository.save(d2);
            List<Deal> dList2 = new ArrayList<>();
            dList2.add(d2);
            vendor2.setDeals_list(dList2);
            vendorRepository.save(vendor2);
        }


        if (restaurantRepository.count() == 0) {
            Restaurant r1 = new Restaurant();
            r1.setName("The Kitchen Table");
            r1.setDescription("The Kitchen Table brings to life a culmination of culinary finesse. Step through our doors and satisfy your appetite anytime of the day! ");
            r1.setAddress("21 Ocean Way Sentosa, 098374");
            r1.setOpening_hours("6.30am - 10pm");
            r1.setContact_num("68087777");
            r1.setIs_published(true);
            r1.setSuggested_duration(3);
            r1.setRestaurant_type(RestaurantEnum.WESTERN);
            r1.setGeneric_location(GenericLocationEnum.SENTOSA);
            r1.setEstimated_price_tier(PriceTierEnum.TIER_5);
            List<String> imgList = new ArrayList<>();
            imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/kitchen1.jpeg");
            imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/kitchen2.jpeg");
            imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/kitchen3.jpeg");
            r1.setRestaurant_image_list(imgList);

            Dish d1 = new Dish();
            d1.setName("BBQ Chicken Pizza");
            d1.setSpicy(false);
            d1.setIs_signature(false);
            d1.setDish_type(DishTypeEnum.MAINS);
            d1.setPrice(new BigDecimal(30));
            d1 = dishRepository.save(d1);

            Dish d2 = new Dish();
            d2.setName("Singapore Chili Lobster");
            d2.setSpicy(true);
            d2.setIs_signature(true);
            d2.setDish_type(DishTypeEnum.MAINS);
            d2.setPrice(new BigDecimal(80));
            d2 = dishRepository.save(d2);

            Dish d3 = new Dish();
            d3.setName("Vegan Mediterranean Bowl");
            d3.setSpicy(false);
            d3.setIs_signature(false);
            d3.setDish_type(DishTypeEnum.SIDES);
            d3.setPrice(new BigDecimal(20));
            d3 = dishRepository.save(d3);

            Dish d4 = new Dish();
            d4.setName("Ibérico Pork Jowl");
            d4.setSpicy(false);
            d4.setIs_signature(true);
            d4.setDish_type(DishTypeEnum.MAINS);
            d4.setPrice(new BigDecimal(35));
            d4 = dishRepository.save(d4);

            Dish d5 = new Dish();
            d5.setName("Vanilla Milk Shake");
            d5.setSpicy(false);
            d5.setIs_signature(true);
            d5.setDish_type(DishTypeEnum.DESSERT);
            d5.setPrice(new BigDecimal(15));
            d5 = dishRepository.save(d5);

            r1.setDish_list(new ArrayList<>());
            r1.getDish_list().add(d1);
            r1.getDish_list().add(d2);
            r1.getDish_list().add(d3);
            r1.getDish_list().add(d4);
            r1.getDish_list().add(d5);

            r1 = restaurantRepository.save(r1);
            List<Restaurant> rList = new ArrayList<>();
            rList.add(r1);
            vendor4.setRestaurant_list(rList);
            vendorRepository.save(vendor4);

            secondRestaurant(vendor5);
        }

        if (accommodationRepository.count() == 0) {
            Accommodation a1 = new Accommodation();
            a1.setName("Resorts World Sentosa");
            List<String> list = new ArrayList<>();
            list.add("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/rwshotel1.jpeg");
            list.add("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/rwshotel2.jpeg");
            a1.setAccommodation_image_list(list);
            a1.setDescription("Singapore's best place! World-class attractions Universal Studios Singapore, S.E.A. Aquarium, Adventure Cove Waterpark; 6 unique hotels, finest dining.");
            a1.setAddress("8 Sentosa Gateway, 098269");
            a1.setContact_num("6363 1212");
            a1.setCheck_in_time(LocalDateTime.parse("2023-10-13T16:00:00"));
            a1.setCheck_out_time(LocalDateTime.parse("2023-10-13T12:00:00"));
            a1.setType(AccommodationTypeEnum.HOTEL);
            a1.setGeneric_location(GenericLocationEnum.SENTOSA);
            a1.setIs_published(true);
            a1.setEstimated_price_tier(PriceTierEnum.TIER_5);
            accommodationRepository.save(a1);

            if (vendor1.getAccommodation_list() == null) vendor1.setAccommodation_list(new ArrayList<>());
            vendor1.getAccommodation_list().add(a1);
            vendorRepository.save(vendor1);

            Accommodation a2 = new Accommodation();
            a2.setName("Mangrove Sentosa");
            List<String> list2 = new ArrayList<>();
            list2.add("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/airbnb.jpeg");
            list2.add("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/airbnb2.jpeg");
            a2.setAccommodation_image_list(list2);
            a2.setDescription("Mangrove Sentosa is a 15-story building with 177 separate units and Stay rooms for short-term usage. The first story consists of a welcome lounge, the community desk and a cafe. The two basement stories are filled with an assortment of communal spaces.");
            a2.setAddress("20 Sentosa Ave, Singapore 453532");
            a2.setContact_num("6123 4567");
            a2.setCheck_in_time(LocalDateTime.parse("2023-10-13T16:00:00"));
            a2.setCheck_out_time(LocalDateTime.parse("2023-10-13T13:00:00"));
            a2.setType(AccommodationTypeEnum.AIRBNB);
            a2.setGeneric_location(GenericLocationEnum.SENTOSA);
            a2.setIs_published(true);
            a2.setEstimated_price_tier(PriceTierEnum.TIER_1);
            accommodationRepository.save(a2);

            if (vendor3.getAccommodation_list() == null) vendor3.setAccommodation_list(new ArrayList<>());
            vendor3.getAccommodation_list().add(a2);
            vendorRepository.save(vendor3);

            Room r1 = new Room();
            r1.setRoom_image("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/room/rwsroom1.png");
            r1.setAmenities_description("A two-storey townhouse offering a land view from an outdoor patio and an underwater view of 40,000 fishes below.");
            r1.setNum_of_pax(3);
            r1.setPrice(new BigDecimal(800));
            r1.setQuantity(2);
            r1.setRoom_type(RoomTypeEnum.DELUXE_SUITE);
            roomRepository.save(r1);

            Room r2 = new Room();
            r2.setRoom_image("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/room/rwsroom2.png");
            r2.setAmenities_description("These deluxe rooms extend to the outdoors leaving you immediately at one with nature.");
            r2.setNum_of_pax(2);
            r2.setPrice(new BigDecimal(250));
            r2.setQuantity(2);
            r2.setRoom_type(RoomTypeEnum.STANDARD);
            roomRepository.save(r2);

            Room r3 = new Room();
            r3.setRoom_image("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/room/airbnbroom1.jpeg");
            r3.setAmenities_description("Single bed, Private bath, Cleaning once a week");
            r3.setNum_of_pax(1);
            r3.setPrice(new BigDecimal(70));
            r3.setQuantity(10);
            r3.setRoom_type(RoomTypeEnum.STANDARD);
            roomRepository.save(r3);

            Room r4 = new Room();
            r4.setRoom_image("https://tt02.s3.ap-southeast-1.amazonaws.com/accommodation/room/airbnbroom2.jpeg");
            r4.setAmenities_description("Bunk bed, Private bath, Cleaning once a week!");
            r4.setNum_of_pax(2);
            r4.setPrice(new BigDecimal(50));
            r4.setQuantity(10);
            r4.setRoom_type(RoomTypeEnum.DOUBLE);
            roomRepository.save(r4);

            List<Room> roomList1 = new ArrayList<>();
            roomList1.add(r1);
            roomList1.add(r2);
            a1.setRoom_list(roomList1);
            accommodationRepository.save(a1);

            List<Room> roomList2 = new ArrayList<>();
            roomList2.add(r3);
            roomList2.add(r4);
            a2.setRoom_list(roomList2);
            accommodationRepository.save(a2);
        }

        if (telecomRepository.count() == 0) {
            Telecom t1 = new Telecom();
            t1.setName("M1");
            t1.setDescription("M1 7 Days 50GB plan");
            t1.setPrice(new BigDecimal(50));
            t1.setType(TelecomTypeEnum.PHYSICALSIM);
            t1.setIs_published(true);
            t1.setEstimated_price_tier(PriceTierEnum.TIER_2);
            t1.setNum_of_days_valid(7);
            t1.setData_limit(50);
            t1.setData_limit_category(GBLimitEnum.VALUE_50);
            t1.setPlan_duration_category(NumberOfValidDaysEnum.SEVEN_DAY);
            t1.setImage("http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_7_day.JPG");

            t1 = telecomRepository.save(t1);
            List<Telecom> tList = new ArrayList<>();
            tList.add(t1);
            vendor2.setTelecom_list(tList);
            vendorRepository.save(vendor2);

            Telecom t2 = new Telecom();
            t2.setName("Singtel");
            t2.setDescription("Singtel 14 Days 80GB plan");
            t2.setPrice(new BigDecimal(60));
            t2.setType(TelecomTypeEnum.PHYSICALSIM);
            t2.setIs_published(true);
            t2.setEstimated_price_tier(PriceTierEnum.TIER_3);
            t2.setNum_of_days_valid(14);
            t2.setData_limit(80);
            t1.setPlan_duration_category(NumberOfValidDaysEnum.FOURTEEN_DAY);
            t2.setData_limit_category(GBLimitEnum.VALUE_100);
            t2.setImage("http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_14_day.JPG");

            t2 = telecomRepository.save(t2);
            List<Telecom> tList2 = new ArrayList<>();
            tList2.add(t2);
            vendor6.setTelecom_list(tList2);
            vendorRepository.save(vendor6);
        }

        if (tourTypeRepository.count() == 0) {
            TourType tourType = new TourType();
            tourType.setName("Mega Adventure Tour");
            List<String> imageList = new ArrayList<>();
            imageList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/static/web/mega_tour.jpg");
            tourType.setTour_image_list(imageList);
            tourType.setDescription("Join me on the mega adventure tour where we will embark on thrilling outdoor activities");
            tourType.setPrice(new BigDecimal(10));
            tourType.setRecommended_pax(10);
            tourType.setEstimated_duration(2);
            tourType.setSpecial_note("Avoid wearing loose items like sunglasses");
            tourType.setIs_published(true);
            tourType.setTour_list(new ArrayList<>());
            tourType.setPublishedUpdatedBy(UserTypeEnum.LOCAL);
            tourType = tourTypeRepository.save(tourType);

            local.getTour_type_list().add(tourType);

            Attraction attraction = attractionRepository.findById(1L).get();
            List<TourType> tourTypes = new ArrayList<>();
            tourTypes.add(tourType);
            attraction.setTour_type_list(tourTypes);
            attractionRepository.save(attraction);

            LocalDate currentDate = LocalDate.now();
            LocalDate endDate = LocalDate.of(2023, 11, 20);
            while (!currentDate.isAfter(endDate)) {
                Tour tour1 = new Tour();
                Tour tour2 = new Tour();
                tour1.setDate(currentDate.atStartOfDay().atZone(ZoneId.of("Asia/Singapore")).toLocalDateTime());
                tour1.setStart_time(currentDate.atTime(10, 0));
                tour1.setEnd_time(currentDate.atTime(12, 0));
                tour1.setRemaining_slot(10);

                tour2.setDate(currentDate.atStartOfDay().atZone(ZoneId.of("Asia/Singapore")).toLocalDateTime());
                tour2.setStart_time(currentDate.atTime(13, 0));
                tour2.setEnd_time(currentDate.atTime(15, 0));
                tour2.setRemaining_slot(10);

                tour1 = tourRepository.save(tour1);
                tour2 = tourRepository.save(tour2);

                tourType.getTour_list().add(tour1);
                tourType.getTour_list().add(tour2);

                currentDate = currentDate.plusDays(1);
            }
            tourTypeRepository.save(tourType);

            createSecondTourType(local);
        }

        if (categoryRepository.count() == 0) {
            for (BookingTypeEnum value : BookingTypeEnum.values()) {
                Category category = new Category();
                String categoryName = value.toString().toLowerCase();
                category.setName(categoryName.substring(0, 1).toUpperCase() + categoryName.substring(1));
                category.setCategory_item_list(new ArrayList<>());
                category.setIs_published(true);
                category = categoryRepository.save(category);
                List<CategoryItem> categoryItemList = new ArrayList<>();

                if (value.equals(BookingTypeEnum.ATTRACTION)) {
                    List<Attraction> attractions = attractionRepository.findAll();

                    for (Attraction attraction : attractions) {
                        CategoryItem categoryItem = new CategoryItem();
                        categoryItem.setName(attraction.getName());
                        categoryItem.setImage(attraction.getAttraction_image_list().get(0));
                        categoryItem.setIs_published(true);
                        categoryItem = categoryItemRepository.save(categoryItem);
                        categoryItemList.add(categoryItem);
                    }
                } else if (value.equals(BookingTypeEnum.ACCOMMODATION)) {
                    List<Accommodation> accommodations = accommodationRepository.findAll();

                    for (Accommodation accommodation : accommodations) {
                        CategoryItem categoryItem = new CategoryItem();
                        categoryItem.setName(accommodation.getName());
                        categoryItem.setImage(accommodation.getAccommodation_image_list().get(0));
                        categoryItem.setIs_published(true);
                        categoryItem = categoryItemRepository.save(categoryItem);
                        categoryItemList.add(categoryItem);
                    }
                } else if (value.equals(BookingTypeEnum.TELECOM)) {
                    List<Telecom> telecoms = telecomRepository.findAll();

                    for (Telecom telecom : telecoms) {
                        CategoryItem categoryItem = new CategoryItem();
                        categoryItem.setName(telecom.getName());
                        categoryItem.setImage(telecom.getImage()); // init telecom w an image
                        categoryItem.setIs_published(true);
                        categoryItem = categoryItemRepository.save(categoryItem);
                        categoryItemList.add(categoryItem);
                    }
                } else if (value.equals(BookingTypeEnum.TOUR)) {
                    List<TourType> tourTypes = tourTypeRepository.findAll();

                    for (TourType tourType : tourTypes) {
                        CategoryItem categoryItem = new CategoryItem();
                        categoryItem.setName(tourType.getName());
                        categoryItem.setImage(tourType.getTour_image_list().get(0));
                        categoryItem.setIs_published(true);
                        categoryItem = categoryItemRepository.save(categoryItem);
                        categoryItemList.add(categoryItem);
                    }
                }

                category.getCategory_item_list().addAll(categoryItemList);
                category.setIs_published(true);
                categoryRepository.save(category);
            }

            Category category = new Category();
            category.setName("Restaurant");
            category.setCategory_item_list(new ArrayList<>());
            category.setIs_published(true);
            category = categoryRepository.save(category);
            List<Restaurant> restaurants = restaurantRepository.findAll();
            List<CategoryItem> categoryItemList = new ArrayList<>();

            for (Restaurant restaurant : restaurants) {
                CategoryItem categoryItem = new CategoryItem();
                categoryItem.setName(restaurant.getName());
                categoryItem.setImage(restaurant.getRestaurant_image_list().get(0));
                categoryItem.setIs_published(true);
                categoryItem = categoryItemRepository.save(categoryItem);
                categoryItemList.add(categoryItem);
            }

            category.getCategory_item_list().addAll(categoryItemList);
            category.setIs_published(true);
            categoryRepository.save(category);

            Category category1 = new Category();
            category1.setName("Others"); // for all the misc forum post
            category1.setCategory_item_list(new ArrayList<>());
            category1.setIs_published(true);
            categoryRepository.save(category1);
        }

        if (supportTicketRepository.count() == 0) {
            SupportTicket s1 = new SupportTicket();
            s1.setCreated_time(LocalDateTime.now().minusDays(1).minusHours(1));
            s1.setUpdated_time(LocalDateTime.now().minusDays(1).minusHours(1));
            s1.setDescription("Is it possible to rent a car via your app?");
            s1.setIs_resolved(true);
            s1.setTicket_category(SupportTicketCategoryEnum.GENERAL_ENQUIRY);
            s1.setTicket_type(SupportTicketTypeEnum.ADMIN);
            s1.setSubmitted_user(UserTypeEnum.TOURIST);
            s1.setSubmitted_user_id(3l);
            s1.setSubmitted_user_name("Rowoon");

            List<Reply> replyList = new ArrayList<Reply>();
            Reply r1 = new Reply();
            r1.setCreated_time(LocalDateTime.now().minusHours(2));
            r1.setUpdated_time(LocalDateTime.now().minusHours(2));
            r1.setMessage("Unfortunately, we do not currently support car rentals. You can check out Car Rental SG's website instead. Thank you.");

            InternalStaff internalStaff = internalStaffRepository.findById(1l).get();
            r1.setInternal_staff_user(internalStaff);

            replyRepository.save(r1);
            replyList.add(r1);

            s1.setReply_list(replyList);
            SupportTicket supportTicket = supportTicketRepository.save(s1);

            Tourist tourist = touristRepository.getTouristByUserId(3l);
            List<SupportTicket> list = new ArrayList<>();
            list.add(supportTicket);
            tourist.setSupport_ticket_list(list);
            touristRepository.save(tourist);

            internalStaff.setSupport_ticket_list(list);
            internalStaffRepository.save(internalStaff);
        }
    }

    public void secondRestaurant(Vendor vendor) {
        Restaurant r1 = new Restaurant();
        r1.setName("8 Noodles");
        r1.setDescription("Casual and authentic eatery at Shangri-La Rasa Sentosa that serves Asian noodles and more!");
        r1.setAddress("101 Siloso Road, 098970");
        r1.setOpening_hours("11am - 11pm");
        r1.setContact_num("63711900");
        r1.setIs_published(true);
        r1.setSuggested_duration(3);
        r1.setRestaurant_type(RestaurantEnum.CHINESE);
        r1.setGeneric_location(GenericLocationEnum.SENTOSA);
        r1.setEstimated_price_tier(PriceTierEnum.TIER_3);
        List<String> imgList = new ArrayList<>();
        imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/chinese1.jpeg");
        imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/chinese2.jpeg");
        imgList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/restaurant/init/chinese3.jpeg");
        r1.setRestaurant_image_list(imgList);

        Dish d1 = new Dish();
        d1.setName("Yang Zhou Fried Rice");
        d1.setSpicy(false);
        d1.setIs_signature(true);
        d1.setDish_type(DishTypeEnum.MAINS);
        d1.setPrice(new BigDecimal(18));
        d1 = dishRepository.save(d1);

        Dish d2 = new Dish();
        d2.setName("Crab Meat Rice");
        d2.setSpicy(false);
        d2.setIs_signature(false);
        d2.setDish_type(DishTypeEnum.MAINS);
        d2.setPrice(new BigDecimal(21));
        d2 = dishRepository.save(d2);

        Dish d3 = new Dish();
        d3.setName("Szechuan Prawn");
        d3.setSpicy(true);
        d3.setIs_signature(true);
        d3.setDish_type(DishTypeEnum.MAINS);
        d3.setPrice(new BigDecimal(29));
        d3 = dishRepository.save(d3);

        Dish d4 = new Dish();
        d4.setName("Singapore Nonya Laksa");
        d4.setSpicy(true);
        d4.setIs_signature(true);
        d4.setDish_type(DishTypeEnum.MAINS);
        d4.setPrice(new BigDecimal(20));
        d4 = dishRepository.save(d4);

        Dish d5 = new Dish();
        d5.setName("Poached Baby Bok Choy");
        d5.setSpicy(false);
        d5.setIs_signature(false);
        d5.setDish_type(DishTypeEnum.SIDES);
        d5.setPrice(new BigDecimal(11));
        d5 = dishRepository.save(d5);

        Dish d6 = new Dish();
        d6.setName("Homemade Wanton Noodles");
        d6.setSpicy(false);
        d6.setIs_signature(true);
        d6.setDish_type(DishTypeEnum.MAINS);
        d6.setPrice(new BigDecimal(21));
        d6 = dishRepository.save(d6);

        Dish d7 = new Dish();
        d7.setName("Beef, Chicken or Lamb Satay");
        d7.setSpicy(false);
        d7.setIs_signature(true);
        d7.setDish_type(DishTypeEnum.SIDES);
        d7.setPrice(new BigDecimal(20));
        d7 = dishRepository.save(d7);

        Dish d8 = new Dish();
        d8.setName("Thai Coconut");
        d8.setSpicy(false);
        d8.setIs_signature(false);
        d8.setDish_type(DishTypeEnum.BEVERAGE);
        d8.setPrice(new BigDecimal(8));
        d8 = dishRepository.save(d8);

        Dish d9 = new Dish();
        d9.setName("Barley Water");
        d9.setSpicy(false);
        d9.setIs_signature(false);
        d9.setDish_type(DishTypeEnum.BEVERAGE);
        d9.setPrice(new BigDecimal(4));
        d9 = dishRepository.save(d9);

        Dish d10 = new Dish();
        d10.setName("Seafood Hor Fun");
        d10.setSpicy(false);
        d10.setIs_signature(true);
        d10.setDish_type(DishTypeEnum.MAINS);
        d10.setPrice(new BigDecimal(20));
        d10 = dishRepository.save(d10);

        r1.setDish_list(new ArrayList<>());
        r1.getDish_list().add(d1);
        r1.getDish_list().add(d2);
        r1.getDish_list().add(d3);
        r1.getDish_list().add(d4);
        r1.getDish_list().add(d5);
        r1.getDish_list().add(d6);
        r1.getDish_list().add(d7);
        r1.getDish_list().add(d8);
        r1.getDish_list().add(d9);
        r1.getDish_list().add(d10);

        restaurantRepository.save(r1);
        List<Restaurant> list = new ArrayList<>();
        list.add(r1);
        vendor.setRestaurant_list(list);
        vendorRepository.save(vendor);
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
        t1.setTicket_date(LocalDate.parse("2023-10-27"));
        t1.setTicket_count(5);
        t1.setTicket_type(TicketEnum.ADULT);
        t1 = ticketPerDayRepository.save(t1);

        TicketPerDay t2 = new TicketPerDay();
        t2.setTicket_date(LocalDate.parse("2023-10-27"));
        t2.setTicket_count(5);
        t2.setTicket_type(TicketEnum.CHILD);
        t2 = ticketPerDayRepository.save(t2);

        TicketPerDay t3 = new TicketPerDay();
        t3.setTicket_date(LocalDate.parse("2023-10-27"));
        t3.setTicket_count(5);
        t3.setTicket_type(TicketEnum.ADULT);
        t3 = ticketPerDayRepository.save(t3);

        TicketPerDay t4 = new TicketPerDay();
        t4.setTicket_date(LocalDate.parse("2023-10-27"));
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
        Vendor vendor = vendorRepository.findById(1L).get();
        currentList.add(attraction); // add on to the previous list
        vendor.setAttraction_list(currentList);
        vendorRepository.save(vendor);
    }

    public void createSecondTourType(Local local) {
        TourType secondTourType = new TourType();
        secondTourType.setName("USS Tour");
        List<String> secondImageList = new ArrayList<>();
        secondImageList.add("https://tt02.s3.ap-southeast-1.amazonaws.com/static/web/uss_tour.jpg");
        secondTourType.setTour_image_list(secondImageList);
        secondTourType.setDescription("Join me on the USS tour to explore Southeast Asia's first and only Universal Studios theme");
        secondTourType.setPrice(new BigDecimal(15));
        secondTourType.setRecommended_pax(15);
        secondTourType.setEstimated_duration(3);
        secondTourType.setSpecial_note("Bring along a poncho for water rides");
        secondTourType.setIs_published(true);
        secondTourType.setTour_list(new ArrayList<>());
        secondTourType.setPublishedUpdatedBy(UserTypeEnum.LOCAL);
        secondTourType = tourTypeRepository.save(secondTourType);

        local.getTour_type_list().add(secondTourType);
        localRepository.save(local);

        Attraction secondAttraction = attractionRepository.findById(2L).get();
        List<TourType> secondTourTypes = new ArrayList<>();
        secondTourTypes.add(secondTourType);
        secondAttraction.setTour_type_list(secondTourTypes);
        attractionRepository.save(secondAttraction);

        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = LocalDate.of(2023, 11, 20);
        while (!currentDate.isAfter(endDate)) {
            Tour tour1 = new Tour();
            Tour tour2 = new Tour();
            tour1.setDate(currentDate.atStartOfDay().atZone(ZoneId.of("Asia/Singapore")).toLocalDateTime());
            tour1.setStart_time(currentDate.atTime(10, 0));
            tour1.setEnd_time(currentDate.atTime(12, 0));
            tour1.setRemaining_slot(10);

            tour2.setDate(currentDate.atStartOfDay().atZone(ZoneId.of("Asia/Singapore")).toLocalDateTime());
            tour2.setStart_time(currentDate.atTime(13, 0));
            tour2.setEnd_time(currentDate.atTime(15, 0));
            tour2.setRemaining_slot(10);

            tour1 = tourRepository.save(tour1);
            tour2 = tourRepository.save(tour2);

            secondTourType.getTour_list().add(tour1);
            secondTourType.getTour_list().add(tour2);

            currentDate = currentDate.plusDays(1);
        }
        tourTypeRepository.save(secondTourType);
    }

    Vendor setUpVendor2(Vendor vendor2) {
        vendor2.setBusiness_name("M1");
        vendor2.setPoc_name("Ang Shih Huei");
        vendor2.setPoc_position("Manager");
        vendor2.setCountry_code("+65");
        vendor2.setPoc_mobile_num("96969697");
        vendor2.setWallet_balance(new BigDecimal(0));
        vendor2.setApplication_status(ApplicationStatusEnum.APPROVED);
        vendor2.setVendor_type(VendorEnum.TELECOM);
        vendor2.setService_description("The best telecom service you can find in Singapore! It's M1!");

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", "telecom@gmail.com");
        customer_parameters.put("name", "M1");
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        vendor2.setStripe_account_id(stripe_account_id);

        vendor2 = vendorRepository.save(vendor2);

        VendorStaff vendorStaff = new VendorStaff();
        vendorStaff.setEmail("telecom@gmail.com");
        vendorStaff.setEmail_verified(true);
        vendorStaff.setName("Ang Shih Huei"); //ewww
        vendorStaff.setPassword(passwordEncoder.encode("password1!"));
        vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
        vendorStaff.setIs_blocked(false);
        vendorStaff.setPosition("Manager");
        vendorStaff.setIs_master_account(true);
        vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
        vendorStaff.setVendor(vendor2);
        vendorStaff.setBadge_list(new ArrayList<>());
        vendorStaffRepository.save(vendorStaff);
        log.debug("created Vendor user - {}", vendorStaff);
        return vendor2;
    }

    Vendor setUpVendor3(Vendor vendor) {
        vendor.setBusiness_name("Mangrove Singapore");
        vendor.setPoc_name("Angelene Chan");
        vendor.setPoc_position("Manager");
        vendor.setCountry_code("+65");
        vendor.setPoc_mobile_num("96963457");
        vendor.setWallet_balance(new BigDecimal(0));
        vendor.setApplication_status(ApplicationStatusEnum.APPROVED);
        vendor.setVendor_type(VendorEnum.ACCOMMODATION);
        vendor.setService_description("We are Mangrove Singapore. A global hotel service provider!");

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", "accommodation@gmail.com");
        customer_parameters.put("name", "Mangrove Singapore");
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        vendor.setStripe_account_id(stripe_account_id);

        vendor = vendorRepository.save(vendor);

        VendorStaff vendorStaff = new VendorStaff();
        vendorStaff.setEmail("accommodation@gmail.com");
        vendorStaff.setEmail_verified(true);
        vendorStaff.setName("Angelene Chan");
        vendorStaff.setPassword(passwordEncoder.encode("password1!"));
        vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
        vendorStaff.setIs_blocked(false);
        vendorStaff.setPosition("Manager");
        vendorStaff.setIs_master_account(true);
        vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
        vendorStaff.setVendor(vendor);
        vendorStaff.setBadge_list(new ArrayList<>());
        vendorStaffRepository.save(vendorStaff);
        log.debug("created Vendor user - {}", vendorStaff);
        return vendor;
    }

    Vendor setUpVendor4(Vendor vendor) {
        vendor.setBusiness_name("The Kitchen Table");
        vendor.setPoc_name("Keith Tan");
        vendor.setPoc_position("Manager");
        vendor.setCountry_code("+65");
        vendor.setPoc_mobile_num("96963431");
        vendor.setWallet_balance(new BigDecimal(0));
        vendor.setApplication_status(ApplicationStatusEnum.APPROVED);
        vendor.setVendor_type(VendorEnum.RESTAURANT);
        vendor.setService_description("We are The Kitchen Table! Our speciality is Singapore classic!");

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", "restaurant@gmail.com");
        customer_parameters.put("name", "The Kitchen Table");
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        vendor.setStripe_account_id(stripe_account_id);

        vendor = vendorRepository.save(vendor);

        VendorStaff vendorStaff = new VendorStaff();
        vendorStaff.setEmail("restaurant@gmail.com");
        vendorStaff.setEmail_verified(true);
        vendorStaff.setName("Keith Tan");
        vendorStaff.setPassword(passwordEncoder.encode("password1!"));
        vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
        vendorStaff.setIs_blocked(false);
        vendorStaff.setPosition("Manager");
        vendorStaff.setIs_master_account(true);
        vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
        vendorStaff.setVendor(vendor);
        vendorStaff.setBadge_list(new ArrayList<>());
        vendorStaffRepository.save(vendorStaff);
        log.debug("created Vendor user - {}", vendorStaff);
        return vendor;
    }

    Vendor setUpVendor5(Vendor vendor) {
        vendor.setBusiness_name("8 Noodles");
        vendor.setPoc_name("Tan Boon Heng");
        vendor.setPoc_position("Manager");
        vendor.setCountry_code("+65");
        vendor.setPoc_mobile_num("96963123");
        vendor.setWallet_balance(new BigDecimal(0));
        vendor.setApplication_status(ApplicationStatusEnum.APPROVED);
        vendor.setVendor_type(VendorEnum.RESTAURANT);
        vendor.setService_description("We are 8 Noodles! Try our noodles!");

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", "restaurant2@gmail.com");
        customer_parameters.put("name", "8 Noodles");
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        vendor.setStripe_account_id(stripe_account_id);

        vendor = vendorRepository.save(vendor);

        VendorStaff vendorStaff = new VendorStaff();
        vendorStaff.setEmail("restaurant2@gmail.com");
        vendorStaff.setEmail_verified(true);
        vendorStaff.setName("Tan Boon Heng");
        vendorStaff.setPassword(passwordEncoder.encode("password1!"));
        vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
        vendorStaff.setIs_blocked(false);
        vendorStaff.setPosition("Manager");
        vendorStaff.setIs_master_account(true);
        vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
        vendorStaff.setVendor(vendor);
        vendorStaff.setBadge_list(new ArrayList<>());
        vendorStaffRepository.save(vendorStaff);
        log.debug("created Vendor user - {}", vendorStaff);
        return vendor;
    }

    Vendor setUpVendor6(Vendor vendor) {
        vendor.setBusiness_name("Singtel");
        vendor.setPoc_name("See Heng How");
        vendor.setPoc_position("Manager");
        vendor.setCountry_code("+65");
        vendor.setPoc_mobile_num("96553123");
        vendor.setWallet_balance(new BigDecimal(0));
        vendor.setApplication_status(ApplicationStatusEnum.APPROVED);
        vendor.setVendor_type(VendorEnum.TELECOM);
        vendor.setService_description("Want the best telecom network? Join Singtel!");

        Map<String, Object> customer_parameters = new HashMap<>();
        customer_parameters.put("email", "telecom2@gmail.com");
        customer_parameters.put("name", "Singtel");
        String stripe_account_id = paymentService.createStripeAccount("CUSTOMER", customer_parameters);
        vendor.setStripe_account_id(stripe_account_id);

        vendor = vendorRepository.save(vendor);

        VendorStaff vendorStaff = new VendorStaff();
        vendorStaff.setEmail("telecom2@gmail.com");
        vendorStaff.setEmail_verified(true);
        vendorStaff.setName("See Heng How");
        vendorStaff.setPassword(passwordEncoder.encode("password1!"));
        vendorStaff.setUser_type(UserTypeEnum.VENDOR_STAFF);
        vendorStaff.setIs_blocked(false);
        vendorStaff.setPosition("Manager");
        vendorStaff.setIs_master_account(true);
        vendorStaff.setProfile_pic("https://tt02.s3.ap-southeast-1.amazonaws.com/user/default_profile.jpg");
        vendorStaff.setVendor(vendor);
        vendorStaff.setBadge_list(new ArrayList<>());
        vendorStaffRepository.save(vendorStaff);
        log.debug("created Vendor user - {}", vendorStaff);
        return vendor;
    }
}
