package com.nus.tt02backend.controllers;

import com.nus.tt02backend.dto.AvailableRoomCountResponse;
import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.Accommodation;
import com.nus.tt02backend.models.enums.GenericLocationEnum;
import com.nus.tt02backend.models.enums.RoomTypeEnum;
import com.nus.tt02backend.services.AccommodationService;
import com.nus.tt02backend.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/accommodation")
public class AccommodationController {

    @Autowired
    AccommodationService accommodationService;

    @GetMapping("/getAllAccommodations")
    public ResponseEntity<List<Accommodation>> getAccommodationList() {
        List<Accommodation> accommodationList = accommodationService.retrieveAllAccommodations();
        return ResponseEntity.ok(accommodationList);
    }

    @GetMapping("/getAllPublishedAccommodation") // local n tourist can only view published listings
    public ResponseEntity<List<Accommodation>> getPublishedAccommodationList() {
        List<Accommodation> publishedAccommodationList = accommodationService.retrieveAllPublishedAccommodation();
        return ResponseEntity.ok(publishedAccommodationList);
    }

    @GetMapping("/getAccommodation/{accommodationId}")
    public ResponseEntity<Accommodation> getAccommodation(@PathVariable Long accommodationId) throws NotFoundException {
        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        return ResponseEntity.ok(accommodation);
    }

    @GetMapping("/getAccommodationListByVendor/{vendorStaffId}")
    public ResponseEntity<List<Accommodation>> getAccommodationListByVendor(@PathVariable Long vendorStaffId) throws NotFoundException {
        List<Accommodation> accommodationList = accommodationService.retrieveAllAccommodationsByVendor(vendorStaffId);
        return ResponseEntity.ok(accommodationList);
    }

    @GetMapping("/getAccommodationByVendor/{vendorStaffId}/{accommodationId}")
    public ResponseEntity<Accommodation> getAccommodationByVendor(@PathVariable Long vendorStaffId,@PathVariable Long accommodationId) throws NotFoundException {
        Accommodation accommodation = accommodationService.retrieveAccommodationByVendor(vendorStaffId,accommodationId);
        return ResponseEntity.ok(accommodation);
    }

    @PostMapping("createAccommodation/{vendorStaffId}")
    public ResponseEntity<Accommodation> createAccommodation(@PathVariable Long vendorStaffId , @RequestBody Accommodation accommodationToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        VendorStaff vendorStaff = accommodationService.retrieveVendor(vendorStaffId);
        Accommodation accommodation =  accommodationService.createAccommodation(vendorStaff,accommodationToCreate);
        return ResponseEntity.ok(accommodation);
    }

    @PutMapping("/updateAccommodation/{vendorStaffId}")
    public ResponseEntity<Void> updateAccommodation(@PathVariable Long vendorStaffId ,@RequestBody Accommodation accommodationToUpdate) throws NotFoundException {
        VendorStaff vendorStaff = accommodationService.retrieveVendor(vendorStaffId);
        accommodationService.updateAccommodation(vendorStaff, accommodationToUpdate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getLastAccommodationId")
    public ResponseEntity<?> getLastAccommodationId() {
        try {
            Long lastAccommodationId = accommodationService.getLastAccommodationId();
            return ResponseEntity.ok(lastAccommodationId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/getLastRoomId")
    public ResponseEntity<?> getLastRoomId() {
        try {
            Long lastRoomId = accommodationService.getLastRoomId();
            return ResponseEntity.ok(lastRoomId);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/createRoom/{accommodationId}")
    public ResponseEntity<Room> createRoom(@PathVariable Long accommodationId , @RequestBody Room roomToCreate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        Room room =  accommodationService.createRoom(accommodation,roomToCreate);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/getRoomListByAccommodation/{accommodationId}")
    public ResponseEntity<List<Room>> getRoomListByAccommodation(@PathVariable Long accommodationId) throws NotFoundException {
        List<Room> roomList = accommodationService.getRoomListByAccommodation(accommodationId);
        return ResponseEntity.ok(roomList);
    }

    @GetMapping("/retrieveAccommodationByRoom/{room_id}")
    public ResponseEntity<Accommodation> retrieveAccommodationByRoom(@PathVariable Long room_id) throws BadRequestException ,NotFoundException {
        Accommodation accommodation = accommodationService.retrieveAccommodationByRoom(room_id);
        return ResponseEntity.ok(accommodation);
    }

    @GetMapping("/getRoomTypeByAccommodation/{accommodation_id}")
    public ResponseEntity<List<RoomTypeEnum>> getRoomTypeByAccommodation(@PathVariable Long accommodation_id) throws NotFoundException {
        List<RoomTypeEnum> roomTypes = accommodationService.getRoomTypeByAccommodation(accommodation_id);
        return ResponseEntity.ok(roomTypes);
    }

    @GetMapping("/getNumOfBookingsOnDate/{accommodation_id}/{roomTypeEnum}/{dateTime}")
    public ResponseEntity<Long> getNumOfBookingsOnDate(@PathVariable Long accommodation_id, @PathVariable RoomTypeEnum roomTypeEnum, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) throws BadRequestException ,NotFoundException {
        Long numOfBookings = accommodationService.getNumOfBookingsOnDate(accommodation_id, roomTypeEnum, dateTime);
        return ResponseEntity.ok(numOfBookings);
    }
    @GetMapping("/isRoomAvailableOnDateRange/{accommodation_id}/{roomTypeEnum}/{checkInDateTime}/{checkOutDateTime}")
    public ResponseEntity<Boolean> isRoomAvailableOnDateRange(@PathVariable Long accommodation_id, @PathVariable RoomTypeEnum roomTypeEnum, @PathVariable LocalDateTime checkInDateTime, @PathVariable LocalDateTime checkOutDateTime) throws BadRequestException ,NotFoundException {
        Boolean isRoomAvailable = accommodationService.isRoomAvailableOnDateRange(accommodation_id, roomTypeEnum, checkInDateTime, checkOutDateTime);
        return ResponseEntity.ok(isRoomAvailable);
    }

    @GetMapping("/getMinAvailableRoomsOnDateRange/{accommodation_id}/{roomTypeEnum}/{checkInDateTime}/{checkOutDateTime}")
    public ResponseEntity<Long> getMinAvailableRoomsOnDateRange(@PathVariable Long accommodation_id, @PathVariable RoomTypeEnum roomTypeEnum, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkInDateTime, @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOutDateTime) throws BadRequestException ,NotFoundException {
        Long numRoomsAvailable = accommodationService.getMinAvailableRoomsOnDateRange(accommodation_id, roomTypeEnum, checkInDateTime, checkOutDateTime);
        return ResponseEntity.ok(numRoomsAvailable);
    }

    @PutMapping("/toggleSaveAccommodation/{userId}/{accommodationId}")
    public ResponseEntity<List<Accommodation>> toggleSaveAccommodation(@PathVariable Long userId, @PathVariable Long accommodationId) throws NotFoundException {
        List<Accommodation> list = accommodationService.toggleSaveAccommodation(userId, accommodationId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/getUserSavedAccommodation/{userId}")
    public ResponseEntity<List<Accommodation>> getPublishedAccommodationList(@PathVariable Long userId) throws NotFoundException {
        List<Accommodation> list = accommodationService.getUserSavedAccommodation(userId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/updateRoom/{accommodationId}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long accommodationId , @RequestBody Room roomToUpate)
            throws BadRequestException, IllegalArgumentException, NotFoundException {

        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        Room room =  accommodationService.updateRoom(roomToUpate);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/getNumOf0AvailableRoomsListOnDateRange/{accommodation_id}/{startDate}/{endDate}")
    public ResponseEntity<List<AvailableRoomCountResponse>> getNumOf0AvailableRoomsListOnDateRange(@PathVariable Long accommodation_id, @PathVariable LocalDate startDate, @PathVariable LocalDate endDate) throws BadRequestException, NotFoundException {
        List<AvailableRoomCountResponse> list = accommodationService.getNumOf0AvailableRoomsListOnDateRange(accommodation_id, startDate, endDate);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/nearbyAccommRecommendation/{locationEnum}")
    public ResponseEntity<List<Accommodation>> nearbyAccommRecommendation(@PathVariable GenericLocationEnum locationEnum) throws NotFoundException {
        List<Accommodation> aList = accommodationService.nearbyAccommRecommendation(locationEnum);
        return ResponseEntity.ok(aList);
    }

    @GetMapping("/nearbyAccommRecommendationWithId/{locationEnum}/{accommId}")
    public ResponseEntity<List<Accommodation>> nearbyAccommRecommendationWithId(@PathVariable GenericLocationEnum locationEnum, @PathVariable Long accommId) throws NotFoundException {
        List<Accommodation> aList = accommodationService.nearbyAccommRecommendation(locationEnum,accommId);
        return ResponseEntity.ok(aList);
    }
}
