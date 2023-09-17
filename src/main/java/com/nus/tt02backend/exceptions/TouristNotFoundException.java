package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TouristNotFoundException extends Exception {
    public TouristNotFoundException(String message) {
        super(message);
    }
}
