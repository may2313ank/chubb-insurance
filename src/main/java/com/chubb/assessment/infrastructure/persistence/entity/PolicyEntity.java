package com.chubb.assessment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class PolicyEntity {

    private static final int POLICY_NUMBER_LENGTH = 32;
    private static final int CURRENCY_LENGTH = 3;
    private static final int PREMIUM_PRECISION = 12;
    private static final int PREMIUM_SCALE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "policy_number", nullable = false, unique = true, updatable = false, length = POLICY_NUMBER_LENGTH)
    private String policyNumber;

    @Column(name = "policyholder_name", nullable = false)
    private String policyholderName;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_of_business", nullable = false)
    private LineOfBusiness lineOfBusiness;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyStatus status;

    @Column(name = "premium_amount", nullable = false, precision = PREMIUM_PRECISION, scale = PREMIUM_SCALE)
    private BigDecimal premiumAmount;

    @Column(nullable = false, length = CURRENCY_LENGTH)
    private String currency;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String underwriter;

    @Column(name = "flagged_for_review", nullable = false)
    private boolean flaggedForReview;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyholderName() {
        return policyholderName;
    }

    public void setPolicyholderName(String policyholderName) {
        this.policyholderName = policyholderName;
    }

    public LineOfBusiness getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(LineOfBusiness lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public BigDecimal getPremiumAmount() {
        return premiumAmount;
    }

    public void setPremiumAmount(BigDecimal premiumAmount) {
        this.premiumAmount = premiumAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getUnderwriter() {
        return underwriter;
    }

    public void setUnderwriter(String underwriter) {
        this.underwriter = underwriter;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
