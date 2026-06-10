package com.chubb.assessment.api.dto.request;

import com.chubb.assessment.api.dto.PolicyConstraints;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PolicyFilterRequest(

        @Pattern(regexp = PolicyConstraints.STATUS_PATTERN)
        String status,

        @Pattern(regexp = PolicyConstraints.LINE_OF_BUSINESS_PATTERN)
        String lineOfBusiness,

        @Pattern(regexp = PolicyConstraints.REGION_PATTERN)
        String region,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate effectiveFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate effectiveTo,

        String q) {
}
