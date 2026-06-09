package com.chubb.assessment.api.controller;

import com.chubb.assessment.api.dto.PolicyConstraints;
import com.chubb.assessment.api.dto.request.FlagPoliciesRequest;
import com.chubb.assessment.api.dto.response.FlagPoliciesResponse;
import com.chubb.assessment.api.dto.response.PagedPolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyResponse;
import com.chubb.assessment.api.dto.response.PolicyStatisticsResponse;
import com.chubb.assessment.api.mapper.DomainToResponseDto;
import com.chubb.assessment.api.mapper.RequestDtoToDomain;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyFilter;
import com.chubb.assessment.service.PolicyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping(PolicyController.BASE)
@Validated
public class PolicyController {

    static final String BASE = "/api/v1/policies";
    private static final String BY_ID = "/{id}";
    private static final String FLAG = "/flag";
    private static final String SUMMARY = "/summary";

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PolicyService policyService;
    private final RequestDtoToDomain requestMapper;
    private final DomainToResponseDto responseMapper;

    public PolicyController(PolicyService policyService,
                           RequestDtoToDomain requestMapper,
                           DomainToResponseDto responseMapper) {
        this.policyService = policyService;
        this.requestMapper = requestMapper;
        this.responseMapper = responseMapper;
    }

    @GetMapping
    public ResponseEntity<PagedPolicyResponse> listPolicies(
            @RequestParam(required = false) @Pattern(regexp = PolicyConstraints.STATUS_PATTERN) String status,
            @RequestParam(required = false) @Pattern(regexp = PolicyConstraints.LINE_OF_BUSINESS_PATTERN) String lineOfBusiness,
            @RequestParam(required = false) @Pattern(regexp = PolicyConstraints.REGION_PATTERN) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
            @RequestParam(required = false) String q,
            @PageableDefault(size = DEFAULT_PAGE_SIZE) Pageable pageable) {
        PolicyFilter filter = requestMapper.toPolicyFilter(status, lineOfBusiness, region, effectiveFrom, effectiveTo, q);
        Page<Policy> page = policyService.list(filter, pageable);
        return ResponseEntity.ok(responseMapper.toPagedResponse(page));
    }

    @GetMapping(BY_ID)
    public ResponseEntity<PolicyResponse> getPolicy(@PathVariable UUID id) {
        Policy policy = policyService.getById(id);
        return ResponseEntity.ok(responseMapper.toPolicyResponse(policy));
    }

    @PatchMapping(FLAG)
    public ResponseEntity<FlagPoliciesResponse> flagPolicies(@Valid @RequestBody FlagPoliciesRequest request) {
        FlagResult result = policyService.flagForReview(request.policyIds());
        return ResponseEntity.ok(responseMapper.toFlagResponse(result));
    }

    @GetMapping(SUMMARY)
    public ResponseEntity<PolicyStatisticsResponse> getSummary() {
        return ResponseEntity.ok(responseMapper.toStatisticsResponse(policyService.statistics()));
    }
}
