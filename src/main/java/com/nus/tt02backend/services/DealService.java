package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.repositories.AccommodationRepository;
import com.nus.tt02backend.repositories.DealRepository;
import com.nus.tt02backend.repositories.RoomRepository;
import com.nus.tt02backend.repositories.VendorStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DealService {

    @Autowired
    DealRepository dealRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    public VendorStaff retrieveVendor(Long vendorStaffId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<VendorStaff> vendorOptional = vendorStaffRepository.findById(vendorStaffId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }


    public List<Deal> retrieveAllDealsByVendor(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        if (!vendor.getDeals_list().isEmpty()) {
            vendor.getDeals_list().stream().map(Deal::getDeal_image_list);
            return vendor.getDeals_list();
        } else {
            throw new NotFoundException("Attractions not found!");
        }
    }


    public Deal createDeal(VendorStaff vendorStaff, Deal dealToCreate ) throws BadRequestException {
        Deal deal = dealRepository.getDealsByPromoCode(dealToCreate.getPromo_code());
        if (deal != null) {
            throw new BadRequestException("There is already an DEAL listing with the same PROMO CODE!");
        }

        Deal newDeal = dealRepository.save(dealToCreate);

        Vendor vendor = vendorStaff.getVendor();
        List<Deal> currentList = vendor.getDeals_list();
        currentList.add(newDeal);
        vendor.setDeals_list(currentList);

        vendorStaffRepository.save(vendorStaff);

        return newDeal;
    }


}
