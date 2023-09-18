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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    VendorRepository vendorRepository;

    public BigDecimal getVendorTotalEarnings(Long vendorId) throws BadRequestException {
        BigDecimal sum = new BigDecimal(0);
        Optional<Vendor> vendorOptional = vendorRepository.findById(vendorId);

        if (vendorOptional.isPresent()) {
            Vendor vendor = vendorOptional.get();
            vendor.setVendor_staff_list(null);

            // fetch attractions
            List<Attraction> attractionList = vendor.getAttraction_list();
            for (Attraction a : attractionList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByAttractionId(a.getAttraction_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Telecom> telecomList = vendor.getTelecom_list();
            for (Telecom t : telecomList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByTelecomId(t.getTelecom_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Deal> dealList = vendor.getDeals_list();
            for (Deal d : dealList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByDealId(d.getDeal_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            List<Accommodation> accommodationList = vendor.getAccommodation_list();
            List<Room> roomList = new ArrayList<>();
            for (Accommodation a : accommodationList) {
                List<Room> tempRoom = a.getRoom_list();
                roomList.addAll(tempRoom);
            }

            for (Room r : roomList) {
                Double tempSum = paymentRepository.retrieveSumOfBookingByRoomId(r.getRoom_id());
                sum = sum.add(new BigDecimal(tempSum));
            }

            sum = sum.multiply(new BigDecimal(0.9)); // 10% commission removal
            return sum;
        } else {
            throw new BadRequestException("Vendor not found!");
        }
    }

    public BigDecimal getTourTotalEarningForLocal(Long localId) {
        Double sum = paymentRepository.retrieveTourEarningsByLocalId(localId);
        BigDecimal totalEarned = new BigDecimal(sum * 0.9);
        return totalEarned;
    }
}
