package com.nus.tt02backend.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ToggleBlockException extends Exception {
    public ToggleBlockException(String message) {
        super(message);
    }
}
