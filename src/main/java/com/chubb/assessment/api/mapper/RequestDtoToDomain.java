package com.chubb.assessment.api.mapper;

import com.chubb.assessment.domain.models.PolicyFilter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class RequestDtoToDomain {

    public PolicyFilter toPolicyFilter(String status,
                                       String lineOfBusiness,
                                       String region,
                                       LocalDate effectiveFrom,
                                       LocalDate effectiveTo,
                                       String searchText) {
        return new PolicyFilter(
                Optional.ofNullable(status),
                Optional.ofNullable(lineOfBusiness),
                Optional.ofNullable(region),
                Optional.ofNullable(effectiveFrom),
                Optional.ofNullable(effectiveTo),
                Optional.ofNullable(searchText).filter(text -> !text.isBlank()));
    }
}
