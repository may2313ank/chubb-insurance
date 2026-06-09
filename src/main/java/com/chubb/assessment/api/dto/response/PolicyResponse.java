package com.chubb.assessment.api.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PolicyResponse(
        String policyNumber,
        String policyholderName,
        String lineOfBusiness,
        String status,
        MoneyResponse premium,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        String region,
        String underwriter,
        boolean flaggedForReview,
        Instant createdAt,
        Instant updatedAt) {
}
