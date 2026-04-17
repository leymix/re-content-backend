package com.recontent.backend.audit.mapper;

import com.recontent.backend.audit.dto.AuditLogResponse;
import com.recontent.backend.audit.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLogEntity auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getMetadataJson(),
                auditLog.getCreatedAt()
        );
    }
}
