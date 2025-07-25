package com.example.fx.infrastructure.repo.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringAccountJpaRepository extends JpaRepository<AccountEntity, UUID> {}
