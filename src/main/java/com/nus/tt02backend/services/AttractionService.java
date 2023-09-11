package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Attr;

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

        attractionToCreate.setPrice_list(persisted_price_list); // set the price list w the newly created price objs
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
            attraction.setPrice_list(updatedPriceList);
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

        if (currentUser.getUserTypeEnum().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            return tourist.getAttraction_list();
        } else if (currentUser.getUserTypeEnum().equals(localType)) {
            Local local = findLocal(userId);
            return local.getAttraction_list();
        } else {
            throw new BadRequestException("Current user type not tourist or local");
        }
    }

    public void saveAttractionForTouristAndLocal (Long userId, Long currentAttractionId) throws BadRequestException, NotFoundException {
        Attraction attractionToSave = retrieveAttraction(currentAttractionId);
        if (attractionToSave.getIs_published() == Boolean.FALSE) {
            throw new BadRequestException("Can't save a hidden attraction!"); // shouldn't trigger if thr is a frontend
        }

        UserTypeEnum touristType = UserTypeEnum.TOURIST;
        UserTypeEnum localType = UserTypeEnum.LOCAL;

        User currentUser = findUser(userId);
        if (currentUser.getUserTypeEnum().equals(touristType)) {
            Tourist tourist = findTourist(userId);
            List<Attraction> currentTouristSavedAttractions = tourist.getAttraction_list();

            for (Attraction a : currentTouristSavedAttractions) {
                if (a.getAttraction_id().equals(currentAttractionId)) {
                    throw new BadRequestException("You have already saved this attraction!");
                }
            }

            currentTouristSavedAttractions.add(attractionToSave);
            touristRepository.save(tourist); // update the list of saved attractions

        } else if (currentUser.getUserTypeEnum().equals(localType)) {
            Local local = findLocal(userId);
            List<Attraction> currentLocalSavedAttractions = local.getAttraction_list();

            for (Attraction a : currentLocalSavedAttractions) {
                if (a.getAttraction_id().equals(currentAttractionId)) {
                    throw new BadRequestException("You have already saved this attraction!");
                }
            }

            currentLocalSavedAttractions.add(attractionToSave);
            localRepository.save(local);
        } else {
            throw new BadRequestException("Invalid User Type! Only Local or Tourist can save an attraction!");
        }
    }

}
