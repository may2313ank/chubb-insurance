package com.chubb.assessment.domain.models;

import java.time.LocalDate;

public final class PolicyExpiry {

    public static final long WINDOW_DAYS = 30;

    private PolicyExpiry() {
    }

    public static boolean isExpiringSoon(LocalDate expiryDate, LocalDate today) {
        return !expiryDate.isBefore(today) && !expiryDate.isAfter(today.plusDays(WINDOW_DAYS));
    }
}
