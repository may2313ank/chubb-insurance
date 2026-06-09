package com.chubb.assessment.service;

import com.chubb.assessment.common.exception.ResourceNotFoundException;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyExpiry;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import com.chubb.assessment.infrastructure.persistence.mapper.PolicyMapper;
import com.chubb.assessment.infrastructure.persistence.repository.LineOfBusinessPremium;
import com.chubb.assessment.infrastructure.persistence.repository.PolicyRepository;
import com.chubb.assessment.infrastructure.persistence.repository.PolicySpecificationFactory;
import com.chubb.assessment.infrastructure.persistence.repository.StatusCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PolicyService {

    private static final Logger log = LoggerFactory.getLogger(PolicyService.class);
    private static final String POLICY_NOT_FOUND = "Policy not found: ";

    private final PolicyRepository repository;
    private final PolicyMapper mapper;
    private final PolicySpecificationFactory specificationFactory;

    public PolicyService(PolicyRepository repository,
                         PolicyMapper mapper,
                         PolicySpecificationFactory specificationFactory) {
        this.repository = repository;
        this.mapper = mapper;
        this.specificationFactory = specificationFactory;
    }

    public Page<Policy> list(PolicyFilter filter, Pageable pageable) {
        long start = System.currentTimeMillis();
        Page<Policy> page = repository.findAll(specificationFactory.from(filter), pageable)
                .map(mapper::toDomain);
        log.info("list pageSize={} totalElements={} durationMs={}",
                page.getSize(), page.getTotalElements(), System.currentTimeMillis() - start);
        return page;
    }

    public Policy getById(UUID id) {
        long start = System.currentTimeMillis();
        Policy policy = mapper.toDomain(findOrThrow(id));
        log.info("getById policyId={} durationMs={}", id, System.currentTimeMillis() - start);
        return policy;
    }

    @Transactional
    public FlagResult flagForReview(List<UUID> policyIds) {
        long start = System.currentTimeMillis();
        List<PolicyEntity> found = repository.findAllById(policyIds);
        found.forEach(entity -> entity.setFlaggedForReview(true));
        repository.saveAll(found);
        FlagResult result = new FlagResult(policyIds.size(), found.size(), missingIds(policyIds, found));
        log.info("flagForReview requested={} flagged={} durationMs={}",
                result.requested(), result.flagged(), System.currentTimeMillis() - start);
        return result;
    }

    public PolicyStatistics statistics() {
        long start = System.currentTimeMillis();
        Map<String, Long> countsByStatus = repository.countGroupedByStatus().stream()
                .collect(Collectors.toMap(count -> count.getStatus().getDisplayValue(), StatusCount::getCount));
        Map<String, BigDecimal> premiumByLineOfBusiness = repository.sumPremiumGroupedByLineOfBusiness().stream()
                .collect(Collectors.toMap(row -> row.getLineOfBusiness().getDisplayValue(),
                        LineOfBusinessPremium::getTotalPremium));
        long expiringSoon = repository.countByExpiryDateBetween(
                LocalDate.now(), LocalDate.now().plusDays(PolicyExpiry.WINDOW_DAYS));
        log.info("statistics durationMs={}", System.currentTimeMillis() - start);
        return new PolicyStatistics(countsByStatus, premiumByLineOfBusiness, expiringSoon);
    }

    private PolicyEntity findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(POLICY_NOT_FOUND + id));
    }

    private List<UUID> missingIds(List<UUID> requested, List<PolicyEntity> found) {
        Set<UUID> foundIds = found.stream().map(PolicyEntity::getId).collect(Collectors.toSet());
        return requested.stream().filter(id -> !foundIds.contains(id)).distinct().toList();
    }
}
