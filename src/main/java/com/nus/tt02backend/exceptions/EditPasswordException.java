package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EditPasswordException extends Exception {
    public EditPasswordException(String message) {
        super(message);
    }
}
