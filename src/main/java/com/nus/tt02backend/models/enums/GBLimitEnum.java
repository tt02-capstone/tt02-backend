package com.nus.tt02backend.models.enums;

public enum GBLimitEnum {
    VALUE_10(10),
    VALUE_30(30),
    VALUE_50(50),
    VALUE_100(100),
    UNLIMITED(-1);

    private final Integer value;

    GBLimitEnum(Integer value) {
        this.value = value;
    }

    public String getValue() {
        switch (this) {
            case VALUE_10:
                return "10";
            case VALUE_30:
                return "30";
            case VALUE_50:
                return "50";
            case VALUE_100:
                return "100";
            case UNLIMITED:
                return "UNLIMITED";
            default:
                return String.valueOf(value);
        }
    }}

