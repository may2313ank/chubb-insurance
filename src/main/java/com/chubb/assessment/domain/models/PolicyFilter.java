package com.chubb.assessment.domain.models;

import java.time.LocalDate;
import java.util.Optional;

public record PolicyFilter(
        Optional<String> status,
        Optional<String> lineOfBusiness,
        Optional<String> region,
        Optional<LocalDate> effectiveFrom,
        Optional<LocalDate> effectiveTo,
        Optional<String> searchText) {
}
