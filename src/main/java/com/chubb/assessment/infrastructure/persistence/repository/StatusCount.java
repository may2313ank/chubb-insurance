package com.chubb.assessment.infrastructure.persistence.repository;

import com.chubb.assessment.infrastructure.persistence.entity.PolicyStatus;

public interface StatusCount {

    PolicyStatus getStatus();

    long getCount();
}
