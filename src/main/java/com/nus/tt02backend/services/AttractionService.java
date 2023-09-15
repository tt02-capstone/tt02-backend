package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.TicketEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;

import java.math.BigDecimal;
import java.util.*;
import java.util.List;

@Service
public class AttractionService {
    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    PriceRepository priceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TouristRepository touristRepository;

    @Autowired
    LocalRepository localRepository;

    public User findUser(Long userId) throws NotFoundException {
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else {
                throw new NotFoundException("User not found!");
            }
        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Tourist findTourist(Long touristId) throws NotFoundException {
        try {
            Tourist tourist = touristRepository.getTouristByUserId(touristId);
            if (tourist != null) {
                return tourist;
            } else {
                throw new NotFoundException("Tourist not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public Local findLocal(Long localId) throws NotFoundException {
        try {
            Local local = localRepository.getLocalByUserId(localId);
            if (local != null) {
                return local;
            } else {
                throw new NotFoundException("Local not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public VendorStaff retrieveVendor(Long vendorStaffId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorStaffId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }

    public List<Attraction> retrieveAllAttraction() {
        return attractionRepository.findAll();
    }

    public List<Attraction> retrieveAllPublishedAttraction() { // for mobile view
        List<Attraction> attractionList = attractionRepository.findAll();
        List<Attraction> publishedList = new ArrayList<>();
        for (Attraction a : attractionList) {
            if (a.getIs_published() == Boolean.TRUE) {
                publishedList.add(a);
            }
        }

        return publishedList;
    }

    public Attraction retrieveAttraction(Long attractionId) throws NotFoundException {
        try {
            Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);
            if (attractionOptional.isPresent()) {
                return attractionOptional.get();
            } else {
                throw new NotFoundException("Attraction not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<Attraction> retrieveAllAttractionByVendor(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        if (!vendor.getAttraction_list().isEmpty()) {
            return vendor.getAttraction_list();
        } else {
            throw new NotFoundException("Attractions not found!");
        }
    }

    public Attraction retrieveAttractionByVendor(Long vendorStaffId, Long attractionId) throws NotFoundException {
        List<Attraction> attractionList = retrieveAllAttractionByVendor(vendorStaffId);
        for (Attraction a : attractionList) {
            if (a.getAttraction_id().equals(attractionId)) {
                return a;
            }
        }
        throw new NotFoundException("Attraction not found!"); // if the attraction is not part of vendor's listing
    }

    public PriceTierEnum priceTierEstimation(List<Price> price_list) {
        // do an avg pricing by using 2 adult + 1 child + 1 senior for attractions
        // based on tourist pricing for now + logic can check in the future
        // tier 1 = 0 - 50 , tier 2 = 51 - 100 , tier 3 = 101 - 200, tier 4 > 200

        BigDecimal total = new BigDecimal("0");
        TicketEnum child = TicketEnum.CHILD;
        TicketEnum adult = TicketEnum.ADULT;
        TicketEnum senior = TicketEnum.SENIOR;

        for (Price price : price_list) {
            if (price.getTicket_type().equals(child)) {
                BigDecimal amt = price.getTourist_amount();
                total = total.add(amt);
            } else if (price.getTicket_type().equals(senior)) {
                BigDecimal amt = price.getTourist_amount();
                total = total.add(amt);
            } else if (price.getTicket_type().equals(adult)) {
                BigDecimal amt = price.getTourist_amount();
                BigDecimal two = new BigDecimal("2");
                BigDecimal amt2 = amt.multiply(two);
                total = total.add(amt2);
            }
        }

        BigDecimal tier1Min = new BigDecimal("0");
        BigDecimal tier1 = new BigDecimal("50");
        BigDecimal tier2 = new BigDecimal("100");
        BigDecimal tier3 = new BigDecimal("150");
        BigDecimal tier4 = new BigDecimal("200");

        if (total.compareTo(tier1Min) >= 0 && total.compareTo(tier1) <= 0) {
            return PriceTierEnum.TIER_1;
        } else if (total.compareTo(tier1) >= 0 && total.compareTo(tier2) <= 0 ) {
            return PriceTierEnum.TIER_2;
        } else if (total.compareTo(tier2) >= 0 && total.compareTo(tier3) <= 0) {
            return PriceTierEnum.TIER_3;
        } else if (total.compareTo(tier3) >= 0 && total.compareTo(tier4) <= 0) {
            return PriceTierEnum.TIER_4;
        } else {
            return PriceTierEnum.TIER_5;
        }
    }

    public List<Price> createPriceList(List<Price> price_list) {
        List<Price> create_price_list = new ArrayList<Price>();

        for (Price input : price_list) {
            Price price = new Price();
            price.setLocal_amount(input.getLocal_amount());
            price.setTourist_amount(input.getTourist_amount());
            price.setTicket_type(input.getTicket_type());
            priceRepository.save(price);

            create_price_list.add(price);
        }

        return create_price_list;
    }

    public List<Price> updatePriceList(List<Price> price_list) throws NotFoundException {
        List<Price> update_price_list = new ArrayList<Price>();

        for (Price input : price_list) {
            Price price = priceRepository.findById(input.getPrice_id()).orElseThrow(() -> new NotFoundException("Pricing Not Found!"));

            price.setLocal_amount(input.getLocal_amount());
            price.setTourist_amount(input.getTourist_amount());
            price.setTicket_type(input.getTicket_type());
            priceRepository.save(price);

            update_price_list.add(price);
        }

        return update_price_list;
    }

    public Attraction createAttraction(VendorStaff vendorStaff, Attraction attractionToCreate) throws BadRequestException {
        Attraction attraction = attractionRepository.getAttractionByName((attractionToCreate.getName()));

        if (attraction != null) {
            throw new BadRequestException("There is an attraction listing with the same name, please another name!");
        }

        List<Price> price_list = attractionToCreate.getPrice_list(); // get the price list and process them as price obj
        List<Price> persisted_price_list = createPriceList(price_list);
        PriceTierEnum priceTier = priceTierEstimation(persisted_price_list);

        attractionToCreate.setPrice_list(persisted_price_list); // set the price list w the newly created price objs
        attractionToCreate.setEstimated_price_tier(priceTier);
        Attraction newAttraction = attractionRepository.save(attractionToCreate);

        Vendor vendor = vendorStaff.getVendor();
        List<Attraction> currentList = vendor.getAttraction_list();
        currentList.add(newAttraction);
        vendor.setAttraction_list(currentList); // set new attraction for the vendor
        System.out.println(vendor.getAttraction_list());

        vendor.setAccommodation_list(null);
        vendor.setWithdrawal_list(null);
        vendor.setVendor_staff_list(null);
//        vendor.setComment_list(null);
//        vendor.setPost_list(null);
        vendor.setRestaurant_list(null);
        vendor.setTelecom_list(null);
        vendor.setDeals_list(null);

        vendorStaffRepository.save(vendorStaff); // update the vendor staff db

        return newAttraction;
    }

    public void updateAttraction(VendorStaff vendorStaff, Attraction attractionToUpdate) throws NotFoundException {
        Attraction attraction = attractionRepository.findById(attractionToUpdate.getAttraction_id())
                .orElseThrow(() -> new NotFoundException("Attraction Not Found!"));
        if (attractionToUpdate.getOpening_hours() != null && attractionToUpdate.getContact_num() != null &&
                attractionToUpdate.getIs_published() != null && !attractionToUpdate.getPrice_list().isEmpty()) {

            attraction.setOpening_hours(attractionToUpdate.getOpening_hours());
            attraction.setContact_num(attractionToUpdate.getContact_num());
            attraction.setIs_published(attractionToUpdate.getIs_published());

            List<Price> updatedPriceList = updatePriceList(attractionToUpdate.getPrice_list());
            PriceTierEnum updatedTier = priceTierEstimation(updatedPriceList);

            attraction.setPrice_list(updatedPriceList);
            attraction.setEstimated_price_tier(updatedTier);
        }

        attractionRepository.save(attraction);
    }

    public List<Attraction> relatedAttractionRecommendation (Long currentAttractionId) throws NotFoundException {
        Attraction currentAttraction = retrieveAttraction(currentAttractionId);
        GenericLocationEnum location = currentAttraction.getGeneric_location();

        List<Attraction> publishedList = retrieveAllPublishedAttraction(); // recommendation cannot be based on hidden listings
        List<Attraction> recommendedAttractionList = new ArrayList<>();

        if (!publishedList.isEmpty()) {
            for (Attraction a : publishedList) {
                if (!a.getAttraction_id().equals(currentAttractionId) && a.getGeneric_location() == location) {
                    recommendedAttractionList.add(a);
                }
            }
        } else {
            throw new NotFoundException("List of attractions is empty!");
        }

        if (recommendedAttractionList.isEmpty()) {
            throw new NotFoundException("No recommended attractions for this listing!");
        } else {
            Collections.shuffle(recommendedAttractionList, new Random()); // anyhow shuffle so it wont keep return the same things
            return recommendedAttractionList.subList(0,2); // take the first 2 will update accordingly ltr on
        }
    }

    public List<Attraction> retrieveAllSavedAttractionsForTouristAndLocal(Long userId) throws NotFoundException, BadRequestException {
        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);

        if (currentUser.getUser_type().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            return tourist.getAttraction_list();
        } else if (currentUser.getUser_type().equals(localType)) {
            Local local = findLocal(userId);
            return local.getAttraction_list();
        } else {
            throw new BadRequestException("Current user type not tourist or local");
        }
    }

    public User saveAttractionForTouristAndLocal (Long userId, Long currentAttractionId) throws BadRequestException, NotFoundException {
        Attraction attractionToSave = retrieveAttraction(currentAttractionId);
        User currentUser = findUser(userId);
        List<Attraction> currentSavedAttractions = new ArrayList<Attraction>();

        if (!attractionToSave.getIs_published()) {
            throw new BadRequestException("Can't save a hidden attraction!"); // shouldn't trigger if thr is a frontend
        }

        if (currentUser.getUser_type().equals(UserTypeEnum.TOURIST)) {
            currentSavedAttractions = ((Tourist) currentUser).getAttraction_list();
        } else if (currentUser.getUser_type().equals(UserTypeEnum.LOCAL)) {
            currentSavedAttractions = ((Local) currentUser).getAttraction_list();
        } else {
            throw new BadRequestException("Invalid User Type! Only Local or Tourist can save an attraction!");
        }

        for (Attraction a : currentSavedAttractions) {
            if (a.getAttraction_id().equals(currentAttractionId)) {
                throw new BadRequestException("You have already saved this attraction!");
            }
        }

        currentSavedAttractions.add(attractionToSave);
        userRepository.save(currentUser);

        return currentUser;
    }

    public User removeSavedAttractionForTouristAndLocal (Long userId, Long currentAttractionId) throws NotFoundException {
        User currentUser = findUser(userId);
        List<Attraction> currentSavedAttractions = new ArrayList<Attraction>();

        if (currentUser.getUser_type() == UserTypeEnum.TOURIST) {
            Tourist tourist = findTourist(userId);
            currentSavedAttractions = tourist.getAttraction_list();
        } else if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
            Local local = findLocal(userId);
            currentSavedAttractions = local.getAttraction_list();
        }

        for (Attraction a : currentSavedAttractions) {
            if (a.getAttraction_id().equals(currentAttractionId)) {
                currentSavedAttractions.remove(a);
                if (currentUser.getUser_type() == UserTypeEnum.TOURIST) {
                    touristRepository.save((Tourist) currentUser);
                } else if (currentUser.getUser_type() == UserTypeEnum.LOCAL) {
                    localRepository.save((Local) currentUser);
                }
                return currentUser;
            }
        }

        throw new NotFoundException("Attraction not found in the saved list!");
    }

}
