package com.smartsyatem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartsyatem.entity.Batch;

public interface BatchRepository extends JpaRepository<Batch, String> {
}