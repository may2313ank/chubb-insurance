package com.chubb.assessment.api.mapper;

import com.chubb.assessment.api.dto.response.FlagPoliciesResponse;
import com.chubb.assessment.api.dto.response.MoneyResponse;
import com.chubb.assessment.api.dto.response.PagedPolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyStatisticsResponse;
import com.chubb.assessment.api.dto.response.PolicySummaryResponse;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyExpiry;
import com.chubb.assessment.domain.models.PolicyStatistics;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DomainToResponseDto {

    public PagedPolicyResponse toPagedResponse(Page<Policy> page) {
        List<PolicySummaryResponse> content = page.getContent().stream()
                .map(this::toSummaryResponse)
                .toList();
        return new PagedPolicyResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public PolicySummaryResponse toSummaryResponse(Policy policy) {
        return new PolicySummaryResponse(
                policy.policyNumber(),
                policy.policyholderName(),
                policy.lineOfBusiness(),
                policy.status(),
                new MoneyResponse(policy.premiumAmount(), policy.currency()),
                policy.region(),
                policy.effectiveDate(),
                policy.expiryDate(),
                policy.flaggedForReview(),
                PolicyExpiry.isExpiringSoon(policy.expiryDate(), LocalDate.now()));
    }

    public PolicyResponse toPolicyResponse(Policy policy) {
        return new PolicyResponse(
                policy.policyNumber(),
                policy.policyholderName(),
                policy.lineOfBusiness(),
                policy.status(),
                new MoneyResponse(policy.premiumAmount(), policy.currency()),
                policy.effectiveDate(),
                policy.expiryDate(),
                policy.region(),
                policy.underwriter(),
                policy.flaggedForReview(),
                policy.createdAt(),
                policy.updatedAt());
    }

    public FlagPoliciesResponse toFlagResponse(FlagResult result) {
        return new FlagPoliciesResponse(result.requested(), result.flagged(), result.missingPolicyIds());
    }

    public PolicyStatisticsResponse toStatisticsResponse(PolicyStatistics statistics) {
        return new PolicyStatisticsResponse(
                statistics.countsByStatus(),
                statistics.totalPremiumByLineOfBusiness(),
                statistics.expiringSoonCount());
    }
}
