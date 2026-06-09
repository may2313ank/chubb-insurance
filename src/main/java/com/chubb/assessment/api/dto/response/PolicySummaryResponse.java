package com.chubb.assessment.api.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record PolicySummaryResponse(
        String policyNumber,
        String policyholderName,
        String lineOfBusiness,
        String status,
        MoneyResponse premium,
        String region,
        LocalDate effectiveDate,
        LocalDate expiryDate,
        boolean flaggedForReview,
        boolean isExpiringSoon) {
}
