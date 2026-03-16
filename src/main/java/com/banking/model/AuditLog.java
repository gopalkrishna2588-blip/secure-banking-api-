package com.banking.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String entityType;
    private String entityId;
    private String performedBy;
    private String ipAddress;

    @Column(length = 2000)
    private String details;

    private LocalDateTime timestamp;
}