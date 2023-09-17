package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class VendorStaffNotFoundException extends Exception {
    public VendorStaffNotFoundException(String message) {
        super(message);
    }
}
