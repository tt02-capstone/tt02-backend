package com.nus.tt02backend.models.enums;

public enum TelecomTypeEnum {
    ESIM("E-SIM"),
    PHYSICALSIM("PHYSICAL-SIM");

    private final String value;

    TelecomTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

