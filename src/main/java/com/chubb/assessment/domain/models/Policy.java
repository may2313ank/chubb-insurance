package com.chubb.assessment.domain.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record Policy(
        UUID id,
        String policyNumber,
        String policyholderName,
        String lineOfBusiness,
        String status,
        BigDecimal premiumAmount,
        String currency,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String region,
        String underwriter,
        boolean flaggedForReview,
        Instant createdAt,
        Instant updatedAt) {
}
