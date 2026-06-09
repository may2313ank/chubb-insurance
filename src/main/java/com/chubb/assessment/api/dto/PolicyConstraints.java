package com.chubb.assessment.api.dto;

public final class PolicyConstraints {

    public static final int NAME_MAX_LENGTH = 120;
    public static final String LINE_OF_BUSINESS_PATTERN = "Property|Casualty|A&H|Marine";
    public static final String STATUS_PATTERN = "Active|Expired|Pending|Cancelled";
    public static final String CURRENCY_PATTERN = "USD|SGD|HKD|AUD|JPY|THB";
    public static final String REGION_PATTERN =
            "Singapore|Hong Kong|Australia|Japan|Thailand|Indonesia|Malaysia|Philippines";
    public static final String PREMIUM_MIN = "1000";
    public static final String PREMIUM_MAX = "5000000";

    private PolicyConstraints() {
    }
}
