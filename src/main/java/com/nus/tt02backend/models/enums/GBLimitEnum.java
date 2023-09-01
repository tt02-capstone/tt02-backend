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

    public Integer getValue() {
        return value;
    }
}

