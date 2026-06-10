package com.chubb.assessment.common.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void doFilterInternal_noIncomingHeader_generatesAndPropagatesCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] captured = new String[1];
        FilterChain chain = (req, res) ->
                captured[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        filter.doFilter(request, response, chain);

        assertNotNull(captured[0]);
        assertEquals(captured[0], response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void doFilterInternal_incomingHeader_reusesSuppliedCorrelationId() throws Exception {
        String supplied = "test-correlation-id";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, supplied);
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] captured = new String[1];
        FilterChain chain = (req, res) ->
                captured[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

        filter.doFilter(request, response, chain);

        assertEquals(supplied, captured[0]);
        assertEquals(supplied, response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }

    @Test
    void doFilterInternal_afterCompletion_clearsMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
        });

        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    }
}
