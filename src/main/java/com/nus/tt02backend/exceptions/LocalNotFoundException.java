package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LocalNotFoundException extends Exception {
    public LocalNotFoundException(String message) {
        super(message);
    }
}
