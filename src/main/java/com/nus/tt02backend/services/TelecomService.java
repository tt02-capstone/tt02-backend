package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
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
    @Autowired
    UserRepository userRepository;
    @Autowired
    TouristRepository touristRepository;
    @Autowired
    LocalRepository localRepository;

    private String generateImageURL(NumberOfValidDaysEnum e) throws NotFoundException {
        if (e == NumberOfValidDaysEnum.ONE_DAY) {
            return "http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_1_day.JPG";
        } else if (e == NumberOfValidDaysEnum.THREE_DAY) {
            return "http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_3_day.JPG";
        } else if (e == NumberOfValidDaysEnum.SEVEN_DAY) {
            return "http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_7_day.JPG";
        } else if (e == NumberOfValidDaysEnum.FOURTEEN_DAY) {
            return "http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_14_day.JPG";
        } else if (e == NumberOfValidDaysEnum.MORE_THAN_FOURTEEN_DAYS) {
            return "http://tt02.s3-ap-southeast-1.amazonaws.com/static/telecom/telecom_more_than_14_day.JPG";
        } else {
            throw new NotFoundException("Enum not found!");
        }
    }

    private GBLimitEnum generateGBLimitEnum(int data) {
        if (data <= 10) {
            return GBLimitEnum.VALUE_10;
        } else if (data <= 30) {
            return GBLimitEnum.VALUE_30;
        } else if (data <= 50) {
            return GBLimitEnum.VALUE_50;
        } else if (data <= 100) {
            return GBLimitEnum.VALUE_100;
        } else {
            return GBLimitEnum.UNLIMITED;
        }
    }

    private NumberOfValidDaysEnum generateNumValidDays(int num) {
        if (num <= 1) {
            return NumberOfValidDaysEnum.ONE_DAY;
        } else if (num <= 3) {
            return NumberOfValidDaysEnum.THREE_DAY;
        } else if (num <= 7) {
            return NumberOfValidDaysEnum.SEVEN_DAY;
        } else if (num <= 14) {
            return NumberOfValidDaysEnum.FOURTEEN_DAY;
        } else {
            return NumberOfValidDaysEnum.MORE_THAN_FOURTEEN_DAYS;
        }
    }

    private PriceTierEnum generatePriceTier(BigDecimal price) {
        if (price.compareTo(new BigDecimal("20")) == -1 || price.compareTo(new BigDecimal("20")) == 0) {
            return PriceTierEnum.TIER_1;
        } else if (price.compareTo(new BigDecimal("40")) == -1 || price.compareTo(new BigDecimal("40")) == 0) {
            return PriceTierEnum.TIER_2;
        } else if (price.compareTo(new BigDecimal("60")) == -1 || price.compareTo(new BigDecimal("60")) == 0) {
            return PriceTierEnum.TIER_3;
        } else if (price.compareTo(new BigDecimal("80")) == -1 || price.compareTo(new BigDecimal("80")) == 0) {
            return PriceTierEnum.TIER_4;
        } else {
            return PriceTierEnum.TIER_5;
        }
    }

    public Telecom create(Telecom telecom, Long vendorId) throws NotFoundException {

        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            telecom.setEstimated_price_tier(generatePriceTier(telecom.getPrice()));
            telecom.setPlan_duration_category(generateNumValidDays(telecom.getNum_of_days_valid()));
            telecom.setImage(generateImageURL(telecom.getPlan_duration_category()));
            telecom.setData_limit_category(generateGBLimitEnum(telecom.getData_limit()));
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

    public List<Telecom> getPublishedTelecomList() {

        List<Telecom> list = telecomRepository.getPublishedTelecomList();
        return list;
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
            telecom.setNum_of_days_valid(telecomToEdit.getNum_of_days_valid());
            telecom.setData_limit(telecomToEdit.getData_limit());

            telecom.setEstimated_price_tier(generatePriceTier(telecomToEdit.getPrice()));
            telecom.setPlan_duration_category(generateNumValidDays(telecomToEdit.getNum_of_days_valid()));
            telecom.setImage(generateImageURL(telecom.getPlan_duration_category()));
            telecom.setData_limit_category(generateGBLimitEnum(telecomToEdit.getData_limit()));
            telecomRepository.save(telecom);
            return telecom;

        } else {
            throw new NotFoundException("Telecom not found!");
        }
    }

    public List<Telecom> toggleSaveTelecom(Long userId, Long telecomId) throws NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Telecom> telecomOptional = telecomRepository.findById(telecomId);

        if (userOptional.isPresent() && telecomOptional.isPresent()) {
            User user = userOptional.get();
            Telecom telecom = telecomOptional.get();
            if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                if (tourist.getTelecom_list() == null) tourist.setTelecom_list(new ArrayList<>());

                if (tourist.getTelecom_list().contains(telecom)) { // remove from saved listing
                    tourist.getTelecom_list().remove(telecom);
                } else {
                    tourist.getTelecom_list().add(telecom);
                }
                touristRepository.save(tourist);
                return tourist.getTelecom_list();
            } else if (user instanceof Local) {
                Local local = (Local) user;
                if (local.getTelecom_list() == null) local.setTelecom_list(new ArrayList<>());

                if (local.getTelecom_list().contains(telecom)) { // remove from saved listing
                    local.getTelecom_list().remove(telecom);
                } else {
                    local.getTelecom_list().add(telecom);
                }
                localRepository.save(local);
                return local.getTelecom_list();
            } else {
                throw new NotFoundException("User is not tourist or local!");
            }
        } else {
            throw new NotFoundException("User or telecom is not found!");
        }
    }

    public List<Telecom> getUserSavedTelecom(Long userId) throws NotFoundException {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user instanceof Tourist) {
                Tourist tourist = (Tourist) user;
                if (tourist.getTelecom_list() == null) return new ArrayList<>();
                return tourist.getTelecom_list();
            } else if (user instanceof Local) {
                Local local = (Local) user;
                if (local.getTelecom_list() == null) return new ArrayList<>();
                return local.getTelecom_list();
            } else {
                throw new NotFoundException("User is not tourist or local!");
            }
        } else {
            throw new NotFoundException("User not found!");
        }
    }
}
