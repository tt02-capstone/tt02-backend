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

    public TourType updateTourType(TourType tourTypeToUpdate) throws BadRequestException {
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
}
