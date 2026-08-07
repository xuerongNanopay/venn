package com.veen.velocitylimits.entity;

import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "load_fund")
public class LoadFundEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "load_id", nullable = false)
    private String loadId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "load_time", nullable = false)
    private Instant loadTime;

    @Column(nullable = false)
    private boolean accepted;

    // protected LoadRecordEntity() {}

    public LoadFundEntity(
        String loadId,
        String customerId,
        BigDecimal amount,
        Instant loadTime
    ) {
        this.loadId = loadId;
        this.customerId = customerId;
        this.amount = amount;
        this.loadTime = loadTime;
        this.accepted = false;
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

    public BigDecimal getAmount() {
        return amount;
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
