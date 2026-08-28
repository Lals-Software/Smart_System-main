package com.smartsyatem.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.smartsyatem.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByRoleAndUsername(String role, String username);
    List<UserAccount> findByRole(String role);
    long countByRole(String role);
    @Transactional
    void deleteByRole(String role);
}
