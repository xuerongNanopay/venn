package com.veen.velocitylimits.repository;

import com.veen.velocitylimits.entity.LoadFundEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadRecordRepository extends JpaRepository<LoadFundEntity, Long> {
    
}
