package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.domain.models.PolicyFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicySpecificationFactoryTest {

    private final PolicySpecificationFactory factory = new PolicySpecificationFactory();

    @Test
    void from_emptyFilter_returnsNonNullSpecification() {
        assertNotNull(factory.from(emptyFilter()));
    }

    @Test
    void from_allKnownValues_returnsNonNullSpecification() {
        PolicyFilter filter = new PolicyFilter(
                Optional.of("Active"),
                Optional.of("Property"),
                Optional.of("Singapore"),
                Optional.of(LocalDate.of(2025, 1, 1)),
                Optional.of(LocalDate.of(2025, 12, 31)),
                Optional.of("Tan"));

        assertNotNull(factory.from(filter));
    }

    @Test
    void from_unknownStatus_throwsIllegalArgument() {
        PolicyFilter filter = statusFilter("Unknown");

        assertThrows(IllegalArgumentException.class, () -> factory.from(filter));
    }

    @Test
    void from_unknownLineOfBusiness_throwsIllegalArgument() {
        PolicyFilter filter = new PolicyFilter(Optional.empty(), Optional.of("Aviation"),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> factory.from(filter));
    }

    private PolicyFilter statusFilter(String status) {
        return new PolicyFilter(Optional.of(status), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    private PolicyFilter emptyFilter() {
        return new PolicyFilter(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }
}
