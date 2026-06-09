package com.chubb.assessment.infrastructure.persistence.entity;

import java.util.Optional;
import java.util.stream.Stream;

public enum LineOfBusiness {

    PROPERTY("Property"),
    CASUALTY("Casualty"),
    ACCIDENT_AND_HEALTH("A&H"),
    MARINE("Marine");

    private final String displayValue;

    LineOfBusiness(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static Optional<LineOfBusiness> fromDisplayValue(String value) {
        return Stream.of(values())
                .filter(lineOfBusiness -> lineOfBusiness.displayValue.equals(value))
                .findFirst();
    }
}
