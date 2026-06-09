package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.infrastructure.persistence.entity.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PolicyRepository
        extends JpaRepository<PolicyEntity, UUID>, JpaSpecificationExecutor<PolicyEntity> {

    @Query("select p.status as status, count(p) as count from PolicyEntity p group by p.status")
    List<StatusCount> countGroupedByStatus();

    @Query("""
            select p.lineOfBusiness as lineOfBusiness, sum(p.premiumAmount) as totalPremium
            from PolicyEntity p
            group by p.lineOfBusiness
            """)
    List<LineOfBusinessPremium> sumPremiumGroupedByLineOfBusiness();

    long countByExpiryDateBetween(LocalDate start, LocalDate end);
}
