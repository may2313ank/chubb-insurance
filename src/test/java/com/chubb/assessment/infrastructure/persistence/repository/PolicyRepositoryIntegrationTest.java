package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.infrastructure.persistence.entity.LineOfBusiness;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(PolicySpecificationFactory.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
class PolicyRepositoryIntegrationTest {

    @Autowired
    private PolicyRepository repository;

    @Autowired
    private PolicySpecificationFactory specificationFactory;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void seed() {
        persist("POL-1", "Tan Wei Ming", LineOfBusiness.PROPERTY, PolicyStatus.ACTIVE,
                new BigDecimal("40000.00"), LocalDate.now().plusDays(10));
        persist("POL-2", "Chan Ka Wai", LineOfBusiness.PROPERTY, PolicyStatus.ACTIVE,
                new BigDecimal("60000.00"), LocalDate.now().plusDays(200));
        persist("POL-3", "Sato Haruki", LineOfBusiness.MARINE, PolicyStatus.EXPIRED,
                new BigDecimal("90000.00"), LocalDate.now().minusDays(5));
        entityManager.flush();
    }

    @Test
    void countGroupedByStatus_mixedStatuses_groupsByStatus() {
        Map<PolicyStatus, Long> counts = repository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(StatusCount::getStatus, StatusCount::getCount));

        assertEquals(2L, counts.get(PolicyStatus.ACTIVE));
        assertEquals(1L, counts.get(PolicyStatus.EXPIRED));
    }

    @Test
    void sumPremiumGroupedByLineOfBusiness_sumsPremiumPerLine() {
        Map<LineOfBusiness, BigDecimal> totals = repository.sumPremiumGroupedByLineOfBusiness().stream()
                .collect(Collectors.toMap(LineOfBusinessPremium::getLineOfBusiness,
                        LineOfBusinessPremium::getTotalPremium));

        assertEquals(0, new BigDecimal("100000.00").compareTo(totals.get(LineOfBusiness.PROPERTY)));
        assertEquals(0, new BigDecimal("90000.00").compareTo(totals.get(LineOfBusiness.MARINE)));
    }

    @Test
    void countByExpiryDateBetween_window_countsOnlyPoliciesInWindow() {
        long count = repository.countByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(30));

        assertEquals(1L, count);
    }

    @Test
    void findAll_statusFilter_returnsOnlyMatchingPolicies() {
        PolicyFilter filter = new PolicyFilter(Optional.of("Expired"), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        List<PolicyEntity> result = repository.findAll(specificationFactory.from(filter));

        assertEquals(1, result.size());
        assertEquals("POL-3", result.get(0).getPolicyNumber());
    }

    @Test
    void findAll_searchTextFilter_matchesPolicyholderNameCaseInsensitive() {
        PolicyFilter filter = new PolicyFilter(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("sato"));

        List<PolicyEntity> result = repository.findAll(specificationFactory.from(filter));

        assertEquals(1, result.size());
        assertEquals("POL-3", result.get(0).getPolicyNumber());
    }

    private void persist(String policyNumber, String policyholder, LineOfBusiness lineOfBusiness,
                         PolicyStatus status, BigDecimal premium, LocalDate expiryDate) {
        PolicyEntity entity = new PolicyEntity();
        entity.setPolicyNumber(policyNumber);
        entity.setPolicyholderName(policyholder);
        entity.setLineOfBusiness(lineOfBusiness);
        entity.setStatus(status);
        entity.setPremiumAmount(premium);
        entity.setCurrency("SGD");
        entity.setEffectiveDate(expiryDate.minusYears(1));
        entity.setExpiryDate(expiryDate);
        entity.setRegion("Singapore");
        entity.setUnderwriter("Lim Hui Ling");
        entity.setFlaggedForReview(false);
        entityManager.persist(entity);
    }
}
