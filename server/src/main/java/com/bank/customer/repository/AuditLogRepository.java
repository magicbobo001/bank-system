// src/main/java/com/bank/customer/repository/AuditLogRepository.java

package com.bank.customer.repository;

import com.bank.customer.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}