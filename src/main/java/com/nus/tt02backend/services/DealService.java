package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.repositories.*;
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
    VendorRepository vendorRepository;

    public Vendor retrieveVendor(Long vendorId) throws IllegalArgumentException, NotFoundException {
        try {
            Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);
            if (vendorOptional.isPresent()) {
                return vendorOptional.get();
            } else {
                throw new NotFoundException("Vendor not found!");
            }

        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
        }
    }


    public List<Deal> retrieveAllDealsByVendor(Long vendorId) throws NotFoundException {
        Vendor vendor = retrieveVendor(vendorId);

        if (!vendor.getDeals_list().isEmpty()) {
            vendor.getDeals_list().stream().map(Deal::getDeal_image_list);
            return vendor.getDeals_list();
        } else {
            throw new NotFoundException("Deals not found!");
        }
    }


    public Deal createDeal(Vendor vendor, Deal dealToCreate ) throws BadRequestException {
        Deal deal = dealRepository.getDealsByPromoCode(dealToCreate.getPromo_code());
        if (deal != null) {
            throw new BadRequestException("There is already an DEAL listing with the same PROMO CODE!");
        }

        Deal newDeal = dealRepository.save(dealToCreate);
        List<Deal> currentList = vendor.getDeals_list();
        currentList.add(newDeal);
        vendor.setDeals_list(currentList);

        vendorRepository.save(vendor);

        return newDeal;
    }

    public Deal getDealById(Long dealId) throws NotFoundException {

        Optional<Deal> dealOptional = dealRepository.findById(dealId);
        if (dealOptional.isPresent()) {
            return dealOptional.get();
        } else {
            throw new NotFoundException("Deal not found!");
        }
    }

    public List<Deal> getAllDealList() {
        return dealRepository.findAll();
    }

    public Long getLastDealId() {
        Long lastDealId = dealRepository.findMaxDealId();
        return (lastDealId != null) ? lastDealId : 0L; // Default to 0 if no attractions exist
    }

    public Deal update(Deal dealToEdit) throws NotFoundException {

        Optional<Deal> dealOptional = dealRepository.findById(dealToEdit.getDeal_id());

        if (dealOptional.isPresent()) {
            Deal deal =  dealOptional.get();

            deal.setPromo_code(dealToEdit.getPromo_code());
            deal.setStart_datetime(dealToEdit.getStart_datetime());
            deal.setEnd_datetime(dealToEdit.getEnd_datetime());
            deal.setIs_published(dealToEdit.getIs_published());
            deal.setDeal_type(dealToEdit.getDeal_type());
            deal.setDeal_image_list(dealToEdit.getDeal_image_list());
            deal.setIs_govt_voucher(dealToEdit.getIs_govt_voucher());
            deal.setDiscount_percent(dealToEdit.getDiscount_percent());
            deal.setPublish_date(dealToEdit.getPublish_date());
            dealRepository.save(deal);
            return deal;

        } else {
            throw new NotFoundException("Deal not found!");
        }
    }


}
