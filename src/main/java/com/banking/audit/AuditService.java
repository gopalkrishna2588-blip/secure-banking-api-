package com.banking.audit;

import com.banking.model.AuditLog;
import com.banking.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(String action, String entityType, String entityId,
                       String details, String ipAddress) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String performer = (auth != null && auth.isAuthenticated())
                ? auth.getName() : "ANONYMOUS";

        AuditLog entry = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performer)
                .details(details)
                .ipAddress(ipAddress)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(entry);
        log.info("[AUDIT] {} | {} | {} | by={}", action, entityType, entityId, performer);
    }
}