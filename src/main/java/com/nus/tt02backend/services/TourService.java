package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.models.*;
import com.nus.tt02backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public TourType getTourTypeByTourTypeId(Long tourTypeId) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeId);

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        return tourTypeOptional.get();
    }

    public TourType updateTourType(Long attractionId, TourType tourTypeToUpdate) throws BadRequestException {
        Optional<TourType> tourTypeOptional = tourTypeRepository.findById(tourTypeToUpdate.getTour_type_id());

        if (tourTypeOptional.isEmpty()) {
            throw new BadRequestException("Tour type does not exist!");
        }

        TourType tourType = tourTypeOptional.get();
        tourType.setPrice(tourTypeToUpdate.getPrice());
        tourType.setName(tourTypeToUpdate.getName());
        tourType.setDescription(tourTypeToUpdate.getDescription());
        tourType.getTour_image_list().clear();
        tourType.getTour_image_list().addAll(tourTypeToUpdate.getTour_image_list());
        tourType.setRecommended_pax(tourTypeToUpdate.getRecommended_pax());
        tourType.setSpecial_note(tourTypeToUpdate.getSpecial_note());
        tourType.setEstimated_duration(tourTypeToUpdate.getEstimated_duration());
        tourType.setIs_published(tourTypeToUpdate.getIs_published());
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
                    && existingTour.getEnd_time().isAfter(tourToCreate.getEnd_time()))
            )) {
                throw new BadRequestException("There is an existing tour that clashes with the timeslot!");
            }
        }

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
            if (existingTour.getDate().toLocalDate().equals(tourToUpdate.getDate().toLocalDate())
                    && (existingTour.getStart_time().isBefore(tourToUpdate.getStart_time())
                    && existingTour.getEnd_time().isAfter(tourToUpdate.getStart_time())
                    || (existingTour.getStart_time().isBefore(tourToUpdate.getEnd_time())
                    && existingTour.getEnd_time().isAfter(tourToUpdate.getEnd_time()))
            )) {
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

        Tour tour = tourOptional.get();
        TourType tourType = tourTypeRepository.getTourTypeTiedToTour(tour.getTour_id());

        tourType.getTour_list().remove(tour);
        tourTypeRepository.save(tourType);
        tourRepository.deleteById(tour.getTour_id());

        return "Tour successfully deleted";
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
