package com.chubb.assessment.api.controller;

import com.chubb.assessment.api.exception.GlobalExceptionHandler;
import com.chubb.assessment.api.mapper.DomainToResponseDto;
import com.chubb.assessment.api.mapper.RequestDtoToDomain;
import com.chubb.assessment.common.exception.ResourceNotFoundException;
import com.chubb.assessment.domain.models.FlagResult;
import com.chubb.assessment.domain.models.Policy;
import com.chubb.assessment.domain.models.PolicyStatistics;
import com.chubb.assessment.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
@Import({DomainToResponseDto.class, RequestDtoToDomain.class, GlobalExceptionHandler.class})
class PolicyControllerTest {

    private static final String BASE_PATH = "/api/v1/policies";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PolicyService policyService;

    @Test
    void listPolicies_defaultRequest_returnsPagedSummaries() throws Exception {
        Page<Policy> page = new PageImpl<>(List.of(sampleDomain(UUID.randomUUID())), PageRequest.of(0, 10), 1);
        when(policyService.list(any(), any())).thenReturn(page);

        mockMvc.perform(get(BASE_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].status").value("Active"))
                .andExpect(jsonPath("$.content[0].premium.currency").value("SGD"));
    }

    @Test
    void listPolicies_invalidStatusFilter_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_PATH).param("status", "Unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getPolicy_existingId_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyService.getById(id)).thenReturn(sampleDomain(id));

        mockMvc.perform(get(BASE_PATH + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyNumber").value("POL-000001"))
                .andExpect(jsonPath("$.underwriter").value("Aisha Rahman"));
    }

    @Test
    void getPolicy_unknownId_returnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(policyService.getById(id)).thenThrow(new ResourceNotFoundException("Policy not found: " + id));

        mockMvc.perform(get(BASE_PATH + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getPolicy_malformedId_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_PATH + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void flagPolicies_validIds_returnsCountsAndMissing() throws Exception {
        UUID missing = UUID.randomUUID();
        when(policyService.flagForReview(any())).thenReturn(new FlagResult(2, 1, List.of(missing)));

        mockMvc.perform(patch(BASE_PATH + "/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyIds\":[\"" + UUID.randomUUID() + "\",\"" + missing + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requested").value(2))
                .andExpect(jsonPath("$.flagged").value(1))
                .andExpect(jsonPath("$.missingPolicyIds[0]").value(missing.toString()));
    }

    @Test
    void flagPolicies_emptyIds_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_PATH + "/flag")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"policyIds\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getSummary_returnsAggregatedStatistics() throws Exception {
        when(policyService.statistics()).thenReturn(new PolicyStatistics(
                Map.of("Active", 3L), Map.of("Property", new BigDecimal("5000")), 7));

        mockMvc.perform(get(BASE_PATH + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countsByStatus.Active").value(3))
                .andExpect(jsonPath("$.totalPremiumByLineOfBusiness.Property").value(5000))
                .andExpect(jsonPath("$.expiringSoonCount").value(7));
    }

    private Policy sampleDomain(UUID id) {
        return new Policy(id, "POL-000001", "Wei Lin Tan", "Property", "Active",
                new BigDecimal("12500.00"), "SGD", LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 1, 1), "Singapore", "Aisha Rahman", false,
                Instant.now(), Instant.now());
    }
}
