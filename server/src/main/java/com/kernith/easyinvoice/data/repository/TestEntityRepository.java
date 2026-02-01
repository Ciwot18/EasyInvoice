package com.kernith.easyinvoice.data.repository;

import com.kernith.easyinvoice.data.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}