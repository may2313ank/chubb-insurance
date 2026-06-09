package com.chubb.assessment.service;

import com.chubb.assessment.common.exception.ResourceNotFoundException;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.infrastructure.persistence.entity.LineOfBusiness;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyStatus;
import com.chubb.assessment.infrastructure.persistence.mapper.PolicyMapper;
import com.chubb.assessment.infrastructure.persistence.repository.LineOfBusinessPremium;
import com.chubb.assessment.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.assessment.infrastructure.persistence.repository.PolicySpecificationFactory;
import com.chubb.assessment.infrastructure.persistence.repository.StatusCount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository repository;

    @Mock
    private PolicyMapper mapper;

    @Mock
    private PolicySpecificationFactory specificationFactory;

    @InjectMocks
    private PolicyService service;

    @Test
    void getById_existingPolicy_returnsMappedDomain() {
        UUID id = UUID.randomUUID();
        PolicyEntity entity = new PolicyEntity();
        Policy expected = sampleDomain(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(expected);

        assertSame(expected, service.getById(id));
    }

    @Test
    void getById_missingPolicy_throwsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void list_withFilter_returnsMappedPage() {
        UUID id = UUID.randomUUID();
        PolicyEntity entity = new PolicyEntity();
        Pageable pageable = PageRequest.of(0, 10);
        when(specificationFactory.from(any())).thenReturn(Specification.where(null));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(mapper.toDomain(entity)).thenReturn(sampleDomain(id));

        Page<Policy> page = service.list(emptyFilter(), pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(id, page.getContent().get(0).id());
    }

    @Test
    void flagForReview_someIdsMissing_flagsFoundAndReportsMissing() {
        UUID found = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        PolicyEntity entity = new PolicyEntity();
        entity.setPolicyNumber("POL-000001");
        entity.setFlaggedForReview(false);
        when(repository.findAllById(List.of(found, missing))).thenReturn(List.of(entity));

        FlagResult result = service.flagForReview(List.of(found, missing));

        assertTrue(entity.isFlaggedForReview());
        verify(repository).saveAll(List.of(entity));
        assertEquals(2, result.requested());
        assertEquals(1, result.flagged());
    }

    @Test
    void statistics_aggregatesCountsPremiumAndExpiringSoon() {
        when(repository.countGroupedByStatus()).thenReturn(List.of(statusCount(PolicyStatus.ACTIVE, 3)));
        when(repository.sumPremiumGroupedByLineOfBusiness())
                .thenReturn(List.of(premium(LineOfBusiness.PROPERTY, new BigDecimal("5000"))));
        when(repository.countByExpiryDateBetween(any(), any())).thenReturn(7L);

        PolicyStatistics statistics = service.statistics();

        assertEquals(3L, statistics.countsByStatus().get("Active"));
        assertEquals(new BigDecimal("5000"), statistics.totalPremiumByLineOfBusiness().get("Property"));
        assertEquals(7L, statistics.expiringSoonCount());
    }

    private StatusCount statusCount(PolicyStatus status, long count) {
        return new StatusCount() {
            public PolicyStatus getStatus() {
                return status;
            }

            public long getCount() {
                return count;
            }
        };
    }

    private LineOfBusinessPremium premium(LineOfBusiness lineOfBusiness, BigDecimal total) {
        return new LineOfBusinessPremium() {
            public LineOfBusiness getLineOfBusiness() {
                return lineOfBusiness;
            }

            public BigDecimal getTotalPremium() {
                return total;
            }
        };
    }

    private Policy sampleDomain(UUID id) {
        return new Policy(id, "POL-000001", "Wei Lin Tan", "Property", "Active",
                null, "SGD", null, null, "Singapore", "Aisha Rahman", false, null, null);
    }

    private PolicyFilter emptyFilter() {
        return new PolicyFilter(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }
}
