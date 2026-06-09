package com.chubb.assessment.infrastructure.persistence.mapper;

import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    public Policy toDomain(PolicyEntity entity) {
        return new Policy(
                entity.getId(),
                entity.getPolicyNumber(),
                entity.getPolicyholderName(),
                entity.getLineOfBusiness().getDisplayValue(),
                entity.getStatus().getDisplayValue(),
                entity.getPremiumAmount(),
                entity.getCurrency(),
                entity.getEffectiveDate(),
                entity.getExpiryDate(),
                entity.getRegion(),
                entity.getUnderwriter(),
                entity.isFlaggedForReview(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
