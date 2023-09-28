package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.*;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.PriceTierEnum;
import com.nus.tt02backend.models.enums.AccommodationTypeEnum;
import com.nus.tt02backend.models.enums.RoomTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccommodationService {

    @Autowired
    AccommodationRepository accommodationRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    VendorStaffRepository vendorStaffRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    VendorRepository vendorRepository;

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

    public List<Accommodation> retrieveAllPublishedAccommodation() {
        List<Accommodation> accommodationList = accommodationRepository.findAll();
        List<Accommodation> publishedList = new ArrayList<>();
        for (Accommodation a : accommodationList) {
            if (a.getIs_published() == Boolean.TRUE) {
                publishedList.add(a);
            }
        }

        return publishedList;
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
        throw new NotFoundException("Accommodation not found!"); // if the accommodation is not part of vendor's listing
    }

    public List<Room> updateRoomList(List<Room> room_list) throws NotFoundException {
        List<Room> update_room_list = new ArrayList<Room>();

        if (room_list != null) {
            for (Room input : room_list) {

                if (input.getRoom_id() == null) {
                    Room roomToCreate = new Room();
                    roomToCreate.setAmenities_description(input.getAmenities_description());
                    roomToCreate.setNum_of_pax(input.getNum_of_pax());
                    roomToCreate.setRoom_type(input.getRoom_type());
                    roomToCreate.setPrice(input.getPrice());
                    roomToCreate.setQuantity(input.getQuantity());

                    roomRepository.save(roomToCreate);
                    update_room_list.add(roomToCreate);
                } else {
                    Room room = roomRepository.findById(input.getRoom_id()).orElseThrow(() -> new NotFoundException("Room Not Found!"));
                    room.setAmenities_description(input.getAmenities_description());
                    room.setNum_of_pax(input.getNum_of_pax());
                    room.setRoom_type(input.getRoom_type());
                    room.setPrice(input.getPrice());
                    room.setQuantity(input.getQuantity());

                    roomRepository.save(room);
                    update_room_list.add(room);
                }
            }
        }
        return update_room_list;
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

        //vendor.setAccommodation_list(null);
        vendor.setWithdrawal_list(null);
        vendor.setVendor_staff_list(null);
//        vendor.setComment_list(null);
//        vendor.setPost_list(null);
        vendor.setRestaurant_list(null);
        vendor.setTelecom_list(null);
        vendor.setDeals_list(null);

        vendorRepository.save(vendor);

        vendorStaff.setVendor(vendor);

        vendorStaffRepository.save(vendorStaff); // update the vendor staff db

        return newAccommodation;
    }

    public void updateAccommodation(VendorStaff vendorStaff, Accommodation accommodationToUpdate) throws NotFoundException {
        Accommodation accommodation = accommodationRepository.findById(accommodationToUpdate.getAccommodation_id())
                .orElseThrow(() -> new NotFoundException("Accommodation Not Found!"));
        if (accommodationToUpdate.getName() != null && accommodationToUpdate.getContact_num() != null &&
                accommodationToUpdate.getIs_published() != null) {

            accommodation.setName(accommodationToUpdate.getName());
            accommodation.setDescription(accommodationToUpdate.getDescription());
            accommodation.setAddress(accommodationToUpdate.getAddress());
            accommodation.setContact_num(accommodationToUpdate.getContact_num());
            accommodation.setAccommodation_image_list(accommodationToUpdate.getAccommodation_image_list());
            accommodation.setIs_published(accommodationToUpdate.getIs_published());
            accommodation.setCheck_in_time(accommodationToUpdate.getCheck_in_time());
            accommodation.setCheck_out_time(accommodationToUpdate.getCheck_out_time());
            accommodation.setType(accommodationToUpdate.getType());
            accommodation.setGeneric_location(accommodationToUpdate.getGeneric_location());

            // change price list to room
            List<Room> updatedRoomList = updateRoomList(accommodationToUpdate.getRoom_list());
            PriceTierEnum updatedTier = priceTierEstimation(updatedRoomList);

            accommodation.setRoom_list(updatedRoomList);
            accommodation.setEstimated_price_tier(updatedTier);
        }

        accommodationRepository.save(accommodation);
    }

    public List<Room> createRoomList(List<Room> room_list) throws BadRequestException {

        List<Room> create_room_list = new ArrayList<Room>();

        for (Room input : room_list) {

            Room roomToCreate = new Room();
            roomToCreate.setAmenities_description(input.getAmenities_description());
            roomToCreate.setNum_of_pax(input.getNum_of_pax());
            roomToCreate.setRoom_type(input.getRoom_type());
            roomToCreate.setPrice(input.getPrice());
            roomToCreate.setQuantity(input.getQuantity());

            roomRepository.save(roomToCreate);

            create_room_list.add(roomToCreate);

        }

        // update price tier

        return create_room_list;
    }


    public Room createRoom(Accommodation accommodation, Room roomToCreate) throws BadRequestException {

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
        return (lastAccommodationId != null) ? lastAccommodationId : 0L; // Default to 0 if no accommodations exist
    }

    public List<Room> getRoomListByAccommodation(Long accommodationId) throws NotFoundException {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new NotFoundException("Accommodation not found when getting list of rooms!"));

        if (accommodation.getRoom_list().isEmpty()) {
            throw new NotFoundException("No rooms created for this accommodation listing!");
        }

        return accommodation.getRoom_list();
    }

    public Accommodation retrieveAccommodationByRoom (Long room_id) throws NotFoundException {
        Room room = roomRepository.findById(room_id)
                .orElseThrow(() -> new NotFoundException("Room not found!"));

        List<Accommodation> allAccommodations = retrieveAllAccommodations();

        if (allAccommodations.isEmpty()) {
            throw new NotFoundException("There are no accommodations!");
        }

        for (Accommodation accomm : allAccommodations) {
            List<Room> accommodationRoomList = accomm.getRoom_list();

            if (!accommodationRoomList.isEmpty()) {
                for (Room r : accommodationRoomList) {
                    if (r.getRoom_id().equals(room_id)) {
                        return accomm;
                    }
                }
            }
        }
        throw new NotFoundException("Accommodation not found!");
    }

    // DONE
    public List<RoomTypeEnum> getRoomTypeByAccommodation(Long accommodation_id) throws NotFoundException {
        Accommodation accommodation = accommodationRepository.findById(accommodation_id)
                .orElseThrow(() -> new NotFoundException("Accommodation not found!"));

        List<RoomTypeEnum> roomTypes = new ArrayList<RoomTypeEnum>();
        if (!accommodation.getRoom_list().isEmpty()) {
            for (Room r : accommodation.getRoom_list()) {
                roomTypes.add(r.getRoom_type());
            }
        }

        return roomTypes;
    }

    public Long getNumOfBookingsOnDate(Long accommodation_id, RoomTypeEnum roomType, LocalDateTime roomDateTime) throws NotFoundException, BadRequestException {

        System.out.println("accommodation_id" + accommodation_id);
        Accommodation accommodation = retrieveAccommodation(accommodation_id);

        System.out.println("accommodation" + accommodation);

        List<Booking> allBookings = bookingRepository.findAll();
        List<Booking> accommodationBookings = new ArrayList<Booking>();

        for (Booking b : allBookings) {
            Accommodation accomm = retrieveAccommodationByRoom(b.getRoom().getRoom_id());
            if (accomm.getAccommodation_id().equals(accommodation_id)) {
                accommodationBookings.add(b);
            }
        }

        long bookedRoomsOnThatDate = 0;

        for (Booking b : accommodationBookings) {
            if (b.getRoom().getRoom_type().equals(roomType)) {
                LocalDateTime checkInDateTime = b.getStart_datetime();
                LocalDateTime checkOutDateTime = b.getEnd_datetime();

                if (!roomDateTime.isBefore(checkInDateTime) && !roomDateTime.isAfter(checkOutDateTime)) {
                    System.out.println("roomDateTime is within the date range.");
                    bookedRoomsOnThatDate++;
                } else {
                    System.out.println("roomDateTime is outside the date range.");
                }
            }
        }

        System.out.println("bookedRoomsOnThatDate" + bookedRoomsOnThatDate);
        return bookedRoomsOnThatDate;
    }

    public boolean isRoomAvailableOnDateRange(Long accommodation_id, RoomTypeEnum roomType, LocalDateTime checkInDateTime, LocalDateTime checkOutDateTime) throws NotFoundException, BadRequestException {
        System.out.println("accommodation_id" + accommodation_id);
        Accommodation accommodation = retrieveAccommodation(accommodation_id);

        System.out.println("accommodation" + accommodation);
        List<LocalDate> dateRange = checkInDateTime.toLocalDate().datesUntil(checkOutDateTime.toLocalDate().plusDays(1)).collect(Collectors.toList());

        long totalRoomCount = 0;
        List<Room> roomList = accommodation.getRoom_list();
        for (Room r : roomList) {
            if (r.getRoom_type().equals(roomType)) {
                totalRoomCount = r.getQuantity().longValue();
                break;
            }
        }

        System.out.println("totalRoomCount" + totalRoomCount);

        for (int i = 0; i < dateRange.size(); i++) {
            LocalDate date = dateRange.get(i);
            LocalDateTime roomDateTime;

            if (i == 0) {
                // First day, use check-in time
                roomDateTime = checkInDateTime;
            } else if (i == dateRange.size() - 1) {
                // Last day, use check-out time
                roomDateTime = checkOutDateTime;
            } else {
                // Other days, use midnight
                roomDateTime = date.atStartOfDay();
            }

            Long bookedRoomsOnThatDate = getNumOfBookingsOnDate(accommodation_id, roomType, roomDateTime);

            if (totalRoomCount - bookedRoomsOnThatDate <= 0) {
                return false; // Room not available on at least one day
            }
        }

        return true; // Room is available for the entire date range
    }

    public long getMinAvailableRoomsOnDateRange(Long accommodation_id, RoomTypeEnum roomType, LocalDateTime checkInDateTime, LocalDateTime checkOutDateTime) throws NotFoundException, BadRequestException {
        Accommodation accommodation = retrieveAccommodation(accommodation_id);
        List<LocalDate> dateRange = checkInDateTime.toLocalDate().datesUntil(checkOutDateTime.toLocalDate().plusDays(1)).collect(Collectors.toList());

        long minAvailableRooms = Long.MAX_VALUE; // Initialize with a large value

        for (int i = 0; i < dateRange.size(); i++) {
            LocalDate date = dateRange.get(i);
            LocalDateTime roomDateTime;

            if (i == 0) {
                // First day, use check-in time
                roomDateTime = checkInDateTime;
            } else if (i == dateRange.size() - 1) {
                // Last day, use check-out time
                roomDateTime = checkOutDateTime;
            } else {
                // Other days, use midnight
                roomDateTime = date.atStartOfDay();
            }

            Long bookedRoomsOnThatDate = getNumOfBookingsOnDate(accommodation_id, roomType, roomDateTime);

            long availableRoomsOnThatDate = getTotalRoomCountForType(accommodation, roomType) - bookedRoomsOnThatDate;

            if (availableRoomsOnThatDate < minAvailableRooms) {
                minAvailableRooms = availableRoomsOnThatDate;
            }
        }

        return minAvailableRooms;
    }
    private long getTotalRoomCountForType(Accommodation accommodation, RoomTypeEnum roomType) {
        return accommodation.getRoom_list().stream()
                .filter(room -> room.getRoom_type() == roomType)
                .mapToLong(room -> room.getQuantity().longValue())
                .sum();
    }

}
