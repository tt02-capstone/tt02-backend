package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EditUserException extends Exception {
    public EditUserException(String message) {
        super(message);
    }
}
