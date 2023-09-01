package com.nus.tt02backend.models.enums;

public enum NumberOfValidDaysEnum {
    ONE_DAY("1_DAY"),
    THREE_DAY("3_DAY"),
    SEVEN_DAY("7_DAY"),
    FOURTEEN_DAY("14_DAY"),
    MORE_THAN_FOURTEEN_DAYS("MORE_THAN_14_DAYS");

    private final String value;

    NumberOfValidDaysEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
