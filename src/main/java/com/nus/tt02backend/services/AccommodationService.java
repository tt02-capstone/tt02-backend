package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.AccommodationTypeEnum;
import com.nus.tt02backend.models.enums.TicketEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

@Service
public class AccommodationService {

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    RoomRepository roomRepository;

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

    public List<Accommodation> retrieveAllAccommodations() {
        return accommodationRepository.findAll();
    }

    public Accommodation retrieveAccommodation(Long accommodationId) throws NotFoundException {
        try {
            Optional<Accommodation> accommodationOptional = accommodationRepository.findById(accommodationId);
            if (accommodationOptional.isPresent()) {
                return accommodationOptional.get();
            } else {
                throw new NotFoundException("Accommodation not found!");
            }
        } catch (Exception ex) {
            throw new NotFoundException((ex.getMessage()));
        }
    }

    public List<Accommodation> retrieveAllAccommodationsByVendor(Long vendorStaffId) throws NotFoundException {
        VendorStaff vendorStaff = retrieveVendor(vendorStaffId);
        Vendor vendor = vendorStaff.getVendor();

        if (!vendor.getAccommodation_list().isEmpty()) {
            return vendor.getAccommodation_list();
        } else {
            throw new NotFoundException("Accommodations not found!");
        }
    }

    public Accommodation retrieveAccommodationByVendor(Long vendorStaffId, Long accommodationId) throws NotFoundException {
        List<Accommodation> accommodationList = retrieveAllAccommodationsByVendor(vendorStaffId);
        for (Accommodation a : accommodationList) {
            if (a.getAccommodation_id().equals(accommodationId)) {
                return a;
            }
        }
        throw new NotFoundException("Accommodation not found!"); // if the attraction is not part of vendor's listing
    }

    public Accommodation createAccommodation(VendorStaff vendorStaff, Accommodation accommodationToCreate) throws BadRequestException {

        Accommodation accommodation = accommodationRepository.getAccommodationByName((accommodationToCreate.getName()));

        if (accommodation != null) {
            throw new BadRequestException("There is an accommodation listing with the same name, please choose another name!");
        }

        List<Room> room_list = accommodationToCreate.getRoom_list(); // get the room list and process them as room obj
        if (room_list != null) {
            List<Room> persisted_room_list = createRoomList(room_list);
            PriceTierEnum priceTier = priceTierEstimation(persisted_room_list);

            accommodationToCreate.setRoom_list(persisted_room_list);
            accommodationToCreate.setEstimated_price_tier(priceTier);
        }

        Accommodation newAccommodation = accommodationRepository.save(accommodationToCreate);

        Vendor vendor = vendorStaff.getVendor();
        List<Accommodation> currentList = vendor.getAccommodation_list();
        currentList.add(newAccommodation);
        vendor.setAccommodation_list(currentList); // set new accommodation for the vendor

        vendor.setAttraction_list(null);
        vendor.setWithdrawal_list(null);
        vendor.setVendor_staff_list(null);
//        vendor.setComment_list(null);
//        vendor.setPost_list(null);
        vendor.setRestaurant_list(null);
        vendor.setTelecom_list(null);
        vendor.setDeals_list(null);

        vendorStaffRepository.save(vendorStaff); // update the vendor staff db

        return newAccommodation;
    }

    public List<Room> createRoomList(List<Room> room_list) throws BadRequestException {
        List<Room> create_room_list = new ArrayList<Room>();

        for (Room input : room_list) {

            Room roomToCreate = new Room();

            roomToCreate.setRoom_number(input.getRoom_number());
            roomToCreate.setAmenities_description(input.getAmenities_description());
            roomToCreate.setNum_of_pax(input.getNum_of_pax());
            roomToCreate.setRoom_type(input.getRoom_type());

            // TO EDIT
            // price need to do price tier est ?
            roomToCreate.setPrice(input.getPrice());

            roomRepository.save(roomToCreate);

            create_room_list.add(roomToCreate);
        }

        return create_room_list;
    }

    public Room createRoom(Accommodation accommodation, Room roomToCreate) throws BadRequestException {

        List<Room> existingRoomList = accommodation.getRoom_list();

        if (!existingRoomList.isEmpty()) {
            for (Room roomToCheck : existingRoomList) {
                if (roomToCheck.getRoom_number().equals(roomToCreate.getRoom_number())) {
                    throw new BadRequestException("There is a room listing with the same room number, please choose another number!");
                }
            }
        }

        Room newRoom = roomRepository.save(roomToCreate);

        List<Room> currentList = accommodation.getRoom_list();
        currentList.add(newRoom);

        accommodation.setRoom_list(currentList); // set updated room list for the accommodation
        System.out.println(accommodation.getRoom_list());

        accommodationRepository.save(accommodation); // save the updated accommodation

        return newRoom;
    }

    public PriceTierEnum priceTierEstimation(List<Room> room_list) {
        // getting avg pricing for room for an accommodation
        // can change in the future
        // tier 1 = 0 - 50 , tier 2 = 51 - 100 , tier 3 = 101 - 200, tier 4 > 200

        BigDecimal total = new BigDecimal("0");

        for (Room room : room_list) {
            BigDecimal amt = room.getPrice();
            total = total.add(amt);
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

    public Long getLastAccommodationId() {
        Long lastAccommodationId = accommodationRepository.findMaxAccommodationId();
        return (lastAccommodationId != null) ? lastAccommodationId : 0L; // Default to 0 if no attractions exist
    }

    public List<Room> getRoomListByAccommodation(Long accommodationId) throws NotFoundException {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new NotFoundException("Accommodation not found when getting list of rooms!"));

        if (accommodation.getRoom_list().isEmpty()) {
            throw new NotFoundException("No rooms created for this accommodation listing!");
        }

        return accommodation.getRoom_list();
    }
}
