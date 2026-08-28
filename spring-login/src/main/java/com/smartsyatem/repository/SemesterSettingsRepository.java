package com.smartsyatem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartsyatem.entity.SemesterSettings;

@Repository
public interface SemesterSettingsRepository extends JpaRepository<SemesterSettings, Long> {
}