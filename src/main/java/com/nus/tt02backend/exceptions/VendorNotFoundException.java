package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VendorNotFoundException extends Exception {
    public VendorNotFoundException(String message) {
        super(message);
    }
}
