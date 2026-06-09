package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.infrastructure.persistence.entity.LineOfBusiness;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PolicySpecificationFactory {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LINE_OF_BUSINESS = "lineOfBusiness";
    private static final String FIELD_REGION = "region";
    private static final String FIELD_EFFECTIVE_DATE = "effectiveDate";
    private static final String FIELD_POLICY_NUMBER = "policyNumber";
    private static final String FIELD_POLICYHOLDER_NAME = "policyholderName";
    private static final String FIELD_UNDERWRITER = "underwriter";
    private static final String WILDCARD = "%";
    private static final String UNKNOWN_VALUE = "Unsupported filter value: ";

    public Specification<PolicyEntity> from(PolicyFilter filter) {
        List<Specification<PolicyEntity>> specifications = new ArrayList<>();
        filter.status().ifPresent(value -> specifications.add(statusEquals(value)));
        filter.lineOfBusiness().ifPresent(value -> specifications.add(lineOfBusinessEquals(value)));
        filter.region().ifPresent(value -> specifications.add(regionEquals(value)));
        filter.effectiveFrom().ifPresent(value -> specifications.add(effectiveOnOrAfter(value)));
        filter.effectiveTo().ifPresent(value -> specifications.add(effectiveOnOrBefore(value)));
        filter.searchText().ifPresent(value -> specifications.add(matchesText(value)));
        return Specification.allOf(specifications);
    }

    private Specification<PolicyEntity> statusEquals(String value) {
        PolicyStatus status = PolicyStatus.fromDisplayValue(value)
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_VALUE + value));
        return (root, query, builder) -> builder.equal(root.get(FIELD_STATUS), status);
    }

    private Specification<PolicyEntity> lineOfBusinessEquals(String value) {
        LineOfBusiness lineOfBusiness = LineOfBusiness.fromDisplayValue(value)
                .orElseThrow(() -> new IllegalArgumentException(UNKNOWN_VALUE + value));
        return (root, query, builder) -> builder.equal(root.get(FIELD_LINE_OF_BUSINESS), lineOfBusiness);
    }

    private Specification<PolicyEntity> regionEquals(String value) {
        return (root, query, builder) -> builder.equal(root.get(FIELD_REGION), value);
    }

    private Specification<PolicyEntity> effectiveOnOrAfter(LocalDate value) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), value);
    }

    private Specification<PolicyEntity> effectiveOnOrBefore(LocalDate value) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get(FIELD_EFFECTIVE_DATE), value);
    }

    private Specification<PolicyEntity> matchesText(String value) {
        String pattern = WILDCARD + value.toLowerCase() + WILDCARD;
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get(FIELD_POLICY_NUMBER)), pattern),
                builder.like(builder.lower(root.get(FIELD_POLICYHOLDER_NAME)), pattern),
                builder.like(builder.lower(root.get(FIELD_UNDERWRITER)), pattern));
    }
}
