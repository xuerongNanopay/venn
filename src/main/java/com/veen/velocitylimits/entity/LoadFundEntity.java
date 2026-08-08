package com.veen.velocitylimits.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "load_fund",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_load_fund_load_id_customer_id",
        columnNames = {"load_id", "customer_id"}
    )
)
public class LoadFundEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "load_id", nullable = false)
    private String loadId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "load_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal loadAmount;

    @Column(name = "load_time", nullable = false)
    private Instant loadTime;

    @Column(nullable = false)
    private boolean accepted;

    protected LoadFundEntity() {}

    public LoadFundEntity(
        String loadId,
        String customerId,
        BigDecimal loadAmount,
        Instant loadTime
    ) {
        this.loadId = loadId;
        this.customerId = customerId;
        this.loadAmount = loadAmount;
        this.loadTime = loadTime;
        this.accepted = false;
    }

    public LoadFundEntity(
        String loadId,
        String customerId,
        BigDecimal loadAmount,
        Instant loadTime,
        boolean accepted
    ) {
        this.loadId = loadId;
        this.customerId = customerId;
        this.loadAmount = loadAmount;
        this.loadTime = loadTime;
        this.accepted = accepted;
    }

    public Long getId() {
        return id;
    }

    public String getLoadId() {
        return loadId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public BigDecimal getLoadAmount() {
        return loadAmount;
    }

    public Instant getLoadTime() {
        return loadTime;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void accept() {
        this.accepted = true;
    }

    public void decline() {
        this.accepted = false;
    }
}
