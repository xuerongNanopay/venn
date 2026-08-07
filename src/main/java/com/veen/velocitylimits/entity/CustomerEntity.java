package com.veen.velocitylimits.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "customers", uniqueConstraints = @UniqueConstraint(columnNames = "customer_id"))
public class CustomerEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    protected CustomerEntity() {
    }

    public CustomerEntity(String customerId) {
        this.customerId = customerId;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }
}
