package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AdminNotFoundException extends Exception {
    public AdminNotFoundException(String message) {
        super(message);
    }
}
