package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EditVendorStaffException extends Exception {
    public EditVendorStaffException(String message) {
        super(message);
    }
}
