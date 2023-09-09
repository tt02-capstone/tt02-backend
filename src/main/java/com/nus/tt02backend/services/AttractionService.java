package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.Price;
import com.nus.tt02backend.models.Vendor;
import com.nus.tt02backend.models.VendorStaff;
import com.nus.tt02backend.repositories.PriceRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.nus.tt02backend.models.Attraction;
import com.nus.tt02backend.repositories.AttractionRepository;
import org.springframework.stereotype.Service;

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

    public List<Attraction> retrieveAllAttraction() {
        return attractionRepository.findAll();
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

    public VendorStaff retrieveVendor(Long vendorId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorId);

            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch(Exception ex) {
            throw new NotFoundException(ex.getMessage());
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
        vendor.setComment_list(null);
        vendor.setPost_list(null);
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
}
