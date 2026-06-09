package com.chubb.assessment.api.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record FlagPoliciesRequest(

        @NotEmpty
        List<@NotNull UUID> policyIds) {
}
