package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateLocalException extends Exception {
    public CreateLocalException(String message) {
        super(message);
    }
}
