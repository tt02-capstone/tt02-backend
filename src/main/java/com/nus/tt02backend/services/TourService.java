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
