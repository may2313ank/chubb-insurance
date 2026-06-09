package com.chubb.assessment.infrastructure.persistence.entity;

import java.util.Optional;
import java.util.stream.Stream;

public enum PolicyStatus {

    ACTIVE("Active"),
    EXPIRED("Expired"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String displayValue;

    PolicyStatus(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static Optional<PolicyStatus> fromDisplayValue(String value) {
        return Stream.of(values())
                .filter(status -> status.displayValue.equals(value))
                .findFirst();
    }
}
