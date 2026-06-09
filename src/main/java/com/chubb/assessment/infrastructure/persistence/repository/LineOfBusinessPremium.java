package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.infrastructure.persistence.entity.LineOfBusiness;

import java.math.BigDecimal;

public interface LineOfBusinessPremium {

    LineOfBusiness getLineOfBusiness();

    BigDecimal getTotalPremium();
}
