package com.edmiyatake.yfl_backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoginCodeRepository extends JpaRepository<LoginCode, Long> {
    Optional<LoginCode> findFirstByEmailOrderByCreatedAtDesc(String email);
}