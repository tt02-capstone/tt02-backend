package com.nus.tt02backend.services;

import com.nus.tt02backend.exceptions.BadRequestException;
import com.nus.tt02backend.exceptions.NotFoundException;
import com.nus.tt02backend.models.Tourist;

public interface TouristService {
    public Tourist touristLogin(String email, String password) throws NotFoundException, BadRequestException;
    public void updateTourist(Tourist touristToUpdate) throws NotFoundException;
    public Long createTourist(Tourist touristToCreate) throws BadRequestException;
}
