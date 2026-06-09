package com.chubb.assessment.domain.models;

import java.math.BigDecimal;
import java.util.Map;

public record PolicyStatistics(
        Map<String, Long> countsByStatus,
        Map<String, BigDecimal> totalPremiumByLineOfBusiness,
        long expiringSoonCount) {
}
