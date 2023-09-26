package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.TicketEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TelecomService {
    @Autowired
    TelecomRepository telecomRepository;
    @Autowired
    VendorRepository vendorRepository;

    public Telecom create(Telecom telecom, Long vendorId) throws NotFoundException {

        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            telecomRepository.save(telecom);
            if (vendor.getTelecom_list() == null) vendor.setTelecom_list(new ArrayList<>());
            vendor.getTelecom_list().add(telecom);
            vendorRepository.save(vendor);
            return telecom;

        } else {
            throw new NotFoundException("Vendor not found!");
        }
    }

    public List<Telecom> getAllTelecomList() {
        return telecomRepository.findAll();
    }

    public List<Telecom> getAllAssociatedTelecom(Long vendorId) throws NotFoundException {
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            return vendor.getTelecom_list();

        } else {
            throw new NotFoundException("Vendor not found!");
        }
    }

    public Telecom getTelecomById(Long telecomId) throws NotFoundException {

        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomId);
        if (telecomOptional.isPresent()) {
            return telecomOptional.get();
        } else {
            throw new NotFoundException("Telecom not found!");
        }
    }

    public Telecom update(Telecom telecomToEdit) throws NotFoundException {

        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomToEdit.getTelecom_id());

        if (telecomOptional.isPresent()) {
            Telecom telecom =  telecomOptional.get();

            telecom.setName(telecomToEdit.getName());
            telecom.setDescription(telecomToEdit.getDescription());
            telecom.setPrice(telecomToEdit.getPrice());
            telecom.setIs_published(telecomToEdit.getIs_published());
            telecom.setType(telecomToEdit.getType());
            telecom.setEstimated_price_tier(telecomToEdit.getEstimated_price_tier());
            telecom.setNum_of_days_valid(telecomToEdit.getNum_of_days_valid());
            telecom.setPlan_duration_category(telecomToEdit.getPlan_duration_category());
            telecom.setData_limit(telecomToEdit.getData_limit());
            telecom.setData_limit_category(telecomToEdit.getData_limit_category());
            telecomRepository.save(telecom);
            return telecom;

        } else {
            throw new NotFoundException("Telecom not found!");
        }
    }
}
