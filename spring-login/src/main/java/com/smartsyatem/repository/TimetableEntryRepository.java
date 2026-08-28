package com.smartsyatem.repository;

import com.smartsyatem.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByYear(String year);
    void deleteByYear(String year);
    List<TimetableEntry> findByStaffName(String staffName);
}