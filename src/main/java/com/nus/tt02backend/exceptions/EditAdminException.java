package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EditAdminException extends Exception {
    public EditAdminException(String message) {
        super(message);
    }
}
