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
        switch (this) {
            case ONE_DAY:
                return "1 Day";
            case THREE_DAY:
                return "3 Days";
            case SEVEN_DAY:
                return "7 Days";
            case FOURTEEN_DAY:
                return "14 Days";
            case MORE_THAN_FOURTEEN_DAYS:
                return "More than 14 Days";
            default:
                return value;
        }
    }
}
