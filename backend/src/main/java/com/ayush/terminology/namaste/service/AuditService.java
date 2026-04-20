package com.ayush.terminology.namaste.service;




import com.ayush.terminology.namaste.model.AuditLog;
import com.ayush.terminology.namaste.repo.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(String userId, String patientId, String action,
                          String endpoint, String payloadSummary) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setPatientId(patientId);
        log.setAction(action);
        log.setEndpoint(endpoint);
        log.setPayloadSummary(payloadSummary);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}

