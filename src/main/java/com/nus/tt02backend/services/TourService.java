package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.models.enums.BookingTypeEnum;
import com.nus.tt02backend.models.enums.UserTypeEnum;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TourService {

    @Autowired
    TourRepository tourRepository;

    @Autowired
    TourTypeRepository tourTypeRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    LocalRepository localRepository;

    @Autowired
    BookingRepository bookingRepository;

    public TourType createTourType(Long userId, Long attractionId, TourType tourTypeToCreate)
            throws BadRequestException {
        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);

        if (attractionOptional.isEmpty()) {
            throw new BadRequestException("Attraction does not exist!");
        }

        Local local = localOptional.get();
        Attraction attraction = attractionOptional.get();
        TourType createdTourType = tourTypeRepository.save(tourTypeToCreate);

        local.getTour_type_list().add(createdTourType);
        localRepository.save(local);

        attraction.getTour_type_list().add(createdTourType);
        attractionRepository.save(attraction);

        return createdTourType;
    }

    public List<TourType> getAllTourTypesByLocal(Long userId) throws BadRequestException {
        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isEmpty()) {
            throw new BadRequestException("Local does not exist!");
        }

        return localOptional.get().getTour_type_list();
    }

    public List<TourType> getAllTourTypesCreated() {
        return tourTypeRepository.findAll();
    }

    public TourType getTourTypeByTourTypeId(Long tourTypeId) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeId);

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        return tourTypeOptional.get();
    }

    @Transactional(rollbackFor = Exception.class)
    public TourType updateTourType(Long attractionId, TourType tourTypeToUpdate) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeToUpdate.getTour_type_id());

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        TourType tourType = tourTypeOptional.get();

        Boolean differentDuration = false;
        if (!tourType.getEstimated_duration().equals(tourTypeToUpdate.getEstimated_duration())) {
            differentDuration = true;
        }

        tourType.setPrice(tourTypeToUpdate.getPrice());
        tourType.setName(tourTypeToUpdate.getName());
        tourType.setDescription(tourTypeToUpdate.getDescription());
        tourType.getTour_image_list().clear();
        tourType.getTour_image_list().addAll(tourTypeToUpdate.getTour_image_list());
        tourType.setRecommended_pax(tourTypeToUpdate.getRecommended_pax());
        tourType.setSpecial_note(tourTypeToUpdate.getSpecial_note());
        tourType.setEstimated_duration(tourTypeToUpdate.getEstimated_duration());
        if (tourType.getPublishedUpdatedBy().equals(UserTypeEnum.INTERNAL_STAFF) && !tourType.getIs_published()) {
            // do not update
        } else {
            tourType.setIs_published(tourTypeToUpdate.getIs_published());
            tourType.setPublishedUpdatedBy(UserTypeEnum.LOCAL);
        }
        tourTypeRepository.save(tourType);

        Attraction oldAttraction = attractionRepository.getAttractionTiedToTourType(tourType.getTour_type_id());
        oldAttraction.getTour_type_list().remove(tourType);
        attractionRepository.save(oldAttraction);

        Optional<Attraction> newAttractionOptional = attractionRepository.findById(attractionId);
        if (newAttractionOptional.isEmpty()) {
            throw new BadRequestException("Attraction does not exist!");
        }
        Attraction newAttraction = newAttractionOptional.get();
        newAttraction.getTour_type_list().add(tourType);
        attractionRepository.save(newAttraction);

        if (differentDuration && !tourType.getTour_list().isEmpty()) {
            for (Tour tour : tourType.getTour_list()) {
                tour.setEnd_time(tour.getStart_time().plusHours(tourTypeToUpdate.getEstimated_duration()));
                tourRepository.save(tour);
            }
        }

        if (differentDuration && !tourType.getTour_list().isEmpty()) {
            for (Tour firstTour : tourType.getTour_list()) {
                for (Tour tourToCheck : tourType.getTour_list()) {
                    if (tourToCheck.getTour_id().longValue() != firstTour.getTour_id().longValue()
                            && tourToCheck.getDate().toLocalDate().equals(firstTour.getDate().toLocalDate())
                            && (tourToCheck.getStart_time().isBefore(firstTour.getStart_time())
                            && tourToCheck.getEnd_time().isAfter(firstTour.getStart_time())
                            || (tourToCheck.getStart_time().isBefore(firstTour.getEnd_time())
                            && tourToCheck.getEnd_time().isAfter(firstTour.getEnd_time())
                            || (tourToCheck.getStart_time().isAfter(firstTour.getStart_time())
                            && tourToCheck.getEnd_time().isBefore(firstTour.getEnd_time()))
                            || tourToCheck.getStart_time().isEqual(firstTour.getStart_time())
                    ))) {
                        throw new BadRequestException("Unable to update duration due to clashes in tour timings!");
                    }
                }
            }
        }

        return tourType;
    }

    public TourType adminUpdateTourType(Long tourTypeIdToUpdate, Boolean newPublishedStatus) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeIdToUpdate);
        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }
        TourType tourType = tourTypeOptional.get();

        tourType.setIs_published(newPublishedStatus);
        tourType.setPublishedUpdatedBy(UserTypeEnum.INTERNAL_STAFF);

        tourType = tourTypeRepository.save(tourType);

        return tourType;
    }

    public String deleteTourType(Long tourTypeIdToDelete) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeIdToDelete);

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        TourType tourType = tourTypeOptional.get();
        Attraction attraction = attractionRepository.getAttractionTiedToTourType(tourTypeIdToDelete);
        attraction.getTour_type_list().remove(tourType);
        attractionRepository.save(attraction);

        Local local = localRepository.getLocalTiedToTourType(tourTypeIdToDelete);
        local.getTour_type_list().remove(tourType);
        localRepository.save(local);

        tourTypeRepository.delete(tourType);

        return "Tour type successfully deleted";
    }

    public Long getLastTourTypeId() {
        Long lastTourTypeId = tourTypeRepository.findMaxTourTypeId();
        return (lastTourTypeId != null) ? lastTourTypeId : 0L;
    }

    public Attraction getAttractionForTourTypeId(Long tourTypeId) throws BadRequestException {
        Attraction attraction = attractionRepository.getAttractionTiedToTourType(tourTypeId);

        if (attraction == null) {
            throw new BadRequestException("There is no attraction that contains this tour type!");
        }

        return attraction;
    }

    public Tour createTour(Long tourTypeId, Tour tourToCreate) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeId);

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        TourType tourType = tourTypeOptional.get();

        for (Tour existingTour : tourType.getTour_list()) {
            if (existingTour.getDate().toLocalDate().equals(tourToCreate.getDate().toLocalDate())
                    && (existingTour.getStart_time().isBefore(tourToCreate.getStart_time())
                    && existingTour.getEnd_time().isAfter(tourToCreate.getStart_time())
                    || (existingTour.getStart_time().isBefore(tourToCreate.getEnd_time())
                    && existingTour.getEnd_time().isAfter(tourToCreate.getEnd_time())
                    || (existingTour.getStart_time().isAfter(tourToCreate.getStart_time())
                    && existingTour.getEnd_time().isBefore(tourToCreate.getEnd_time()))
                    || existingTour.getStart_time().isEqual(tourToCreate.getStart_time())
            ))) {
                throw new BadRequestException("There is an existing tour that clashes with the timeslot!");
            }
        }

        tourToCreate.setRemaining_slot(tourType.getRecommended_pax());
        LocalDate date = tourToCreate.getDate().toLocalDate();
        tourToCreate.setDate(date.atStartOfDay().atZone(ZoneId.of("Asia/Singapore")).toLocalDateTime());
        Tour createdTour = tourRepository.save(tourToCreate);
        tourType.getTour_list().add(createdTour);
        tourTypeRepository.save(tourType);

        return createdTour;
    }

    public List<Tour> getAllToursByTourType(Long tourTypeId) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeId);

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        return tourTypeOptional.get().getTour_list();
    }

    public Tour getTourByTourId(Long tourId) throws BadRequestException {
        Optional<Tour> tourOptional = tourRepository.findById(tourId);

        if (tourOptional.isEmpty()) {
            throw new BadRequestException("Tour does not exist!");
        }

        return tourOptional.get();
    }

    public Tour updateTour(Tour tourToUpdate) throws BadRequestException {
        Optional<Tour> tourOptional = tourRepository.findById(tourToUpdate.getTour_id());

        if (tourOptional.isEmpty()) {
            throw new BadRequestException("Tour does not exist!");
        }

        Tour tour = tourOptional.get();
        TourType tourType = tourTypeRepository.getTourTypeTiedToTour(tour.getTour_id());
        for (Tour existingTour : tourType.getTour_list()) {
            if (existingTour.getTour_id().longValue() != tour.getTour_id().longValue()
                    && existingTour.getDate().toLocalDate().equals(tourToUpdate.getDate().toLocalDate())
                    && (existingTour.getStart_time().isBefore(tourToUpdate.getStart_time())
                    && existingTour.getEnd_time().isAfter(tourToUpdate.getStart_time())
                    || (existingTour.getStart_time().isBefore(tourToUpdate.getEnd_time())
                    && existingTour.getEnd_time().isAfter(tourToUpdate.getEnd_time())
                    || (existingTour.getStart_time().isAfter(tourToUpdate.getStart_time())
                    && existingTour.getEnd_time().isBefore(tourToUpdate.getEnd_time()))
                    || existingTour.getStart_time().isEqual(tourToUpdate.getStart_time())
            ))) {
                throw new BadRequestException("There is an existing tour that clashes with the timeslot!");
            }
        }

        tour.setDate(tourToUpdate.getDate());
        tour.setStart_time(tourToUpdate.getStart_time());
        tour.setEnd_time(tourToUpdate.getEnd_time());
        tourRepository.save(tour);

        return tour;
    }

    public String deleteTour(Long tourIdToDelete) throws BadRequestException {
        Optional<Tour> tourOptional = tourRepository.findById(tourIdToDelete);

        if (tourOptional.isEmpty()) {
            throw new BadRequestException("Tour does not exist!");
        }

        List<Booking> bookings = bookingRepository.getAllTourBookings(BookingTypeEnum.TOUR);
        for (Booking booking : bookings) {
            if (booking.getTour().getTour_id().equals(tourIdToDelete)) {
                throw new BadRequestException("Tour cannot be deleted as there are existing bookings under it!");
            }
        }

        Tour tour = tourOptional.get();
        TourType tourType = tourTypeRepository.getTourTypeTiedToTour(tour.getTour_id());

        tourType.getTour_list().remove(tour);
        tourTypeRepository.save(tourType);
        tourRepository.deleteById(tour.getTour_id());

        return "Tour successfully deleted";
    }

    public List<TourType> getAllTourTypesByAttraction(Long attractionId, LocalDateTime dateSelected) throws BadRequestException {
        Optional<Attraction> attractionOptional = attractionRepository.findById(attractionId);

        if (attractionOptional.isEmpty()) {
            throw new BadRequestException("Attraction does not exist!");
        }

        Attraction attraction = attractionOptional.get();
        List<TourType> listOfAllTourTypes = attraction.getTour_type_list();
        List<TourType> listOfAvailableTourTypes = new ArrayList<TourType>();
        for (TourType tourType : listOfAllTourTypes) {
            if (tourType.getIs_published()) {
                List<Tour> listOfAllTours = tourType.getTour_list();
                List<Tour> tempTours = new ArrayList<Tour>();
                Boolean matchingTour = false;
                for (Tour tour : listOfAllTours) {
                    if (tour.getDate().toLocalDate().equals(dateSelected.toLocalDate())) {
                        matchingTour = true;
                        tempTours.add(tour);
                    }
                }

                if (matchingTour) {
                    tourType.getTour_list().clear();
                    tourType.getTour_list().addAll(tempTours);
                    listOfAvailableTourTypes.add(tourType);
                }

                tempTours.clear();
            }
        }

        return listOfAvailableTourTypes;
    }

    public List<Booking> getAllBookingsByLocal(Long userId) throws BadRequestException {
        Optional<Local> localOptional = localRepository.findById(userId);

        if (localOptional.isEmpty()) {
            throw new BadRequestException("User does not exist!");
        }

        Local local = localOptional.get();
        List<Tour> tours = new ArrayList<>();
        for (TourType tourType : local.getTour_type_list()) {
            tours.addAll(tourType.getTour_list());
        }

        List<Booking> bookings = new ArrayList<>();
        List<Booking> listOfAllTourBookings = bookingRepository.getAllTourBookings(BookingTypeEnum.TOUR);
        for (Booking booking : listOfAllTourBookings) {
            if (tours.contains(booking.getTour())) {
                bookings.add(booking);
            }
        }

        for (Booking b : bookings) {
            if (b.getLocal_user() != null) {
                Local localUser = b.getLocal_user();
                localUser.setPost_list(null);
                localUser.setComment_list(null);
                localUser.setCart_list(null);
                localUser.setBooking_list(null);
                localUser.setSupport_ticket_list(null);

            } else if (b.getTourist_user() != null) {
                Tourist touristUser = b.getTourist_user();
                touristUser.setPost_list(null);
                touristUser.setComment_list(null);
                touristUser.setCart_list(null);
                touristUser.setBooking_list(null);
                touristUser.setTour_type_list(null);
                touristUser.setSupport_ticket_list(null);
            }
            b.getPayment().setBooking(null);
        }

        return bookings;
    }

    public Booking getBookingByBookingId(Long bookingId) throws BadRequestException {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isEmpty()) {
            throw new BadRequestException("Booking does not exist!");
        }

        Booking booking = bookingOptional.get();
        if (booking.getLocal_user() != null) {
            Local localUser = booking.getLocal_user();
            localUser.setPost_list(null);
            localUser.setComment_list(null);
            localUser.setCart_list(null);
            localUser.setBooking_list(null);
            localUser.setSupport_ticket_list(null);

        } else if (booking.getTourist_user() != null) {
            Tourist touristUser = booking.getTourist_user();
            touristUser.setPost_list(null);
            touristUser.setComment_list(null);
            touristUser.setCart_list(null);
            touristUser.setBooking_list(null);
            touristUser.setTour_type_list(null);
            touristUser.setSupport_ticket_list(null);
        }
        booking.getPayment().setBooking(null);

        return booking;
    }

    /*
    public Long createTour(Long tourTypeId, Tour tour) throws BadRequestException {

//        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeId);

        if (true) { // to be changed later
            tourRepository.save(tour);
            // to find tour type
            // to add tour to tour type
            // to save tour type
            return tour.getTour_id();
        } else {
            throw new BadRequestException("Tour type not found!");
        }
    }
     */
}
