package com.ayush.terminology.namaste.controller;

import com.ayush.terminology.namaste.mappings.Mapping;
import com.ayush.terminology.namaste.model.*;
import com.ayush.terminology.namaste.repo.*;
import com.ayush.terminology.namaste.service.CSVImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired private CSVImportService csvImportService;
    @Autowired private ImportHistoryRepository importHistoryRepository;
    @Autowired private CodeSystemRepository codeSystemRepository;
    @Autowired private SystemVersionRepository systemVersionRepository;
    @Autowired private MappingRepository mappingRepository;
    @Autowired private ConceptRepository conceptRepository;
    @Autowired private ConceptRequestRepository conceptRequestRepository;
    @Autowired private MlFeedbackRepository mlFeedbackRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    // --- CSV Import ---

    @PostMapping("/namaste/import")
    public ResponseEntity<?> importNamasteCSV(@RequestParam("file") MultipartFile file,
                                               Authentication auth) {
        try {
            csvImportService.importNAMASTECsv(file);
            saveImportHistory(file, "NAMASTE", "SUCCESS", "", auth);
            return ResponseEntity.ok("NAMASTE codes imported successfully");
        } catch (Exception e) {
            saveImportHistory(file, "NAMASTE", "FAILED", e.getMessage(), auth);
            return ResponseEntity.status(400).body("Import failed: " + e.getMessage());
        }
    }

    @PostMapping("/import/concepts/csv")
    public ResponseEntity<?> importConceptsFromCsv(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(required = false) String codeSystem,
                                                    @RequestParam(required = false) List<String> codeSystems,
                                                    @RequestParam(required = false, defaultValue = "") String reportDescription,
                                                    Authentication auth) {
        try {
            String systemName = codeSystem != null ? codeSystem :
                    (codeSystems != null && !codeSystems.isEmpty() ? codeSystems.get(0) : "NAMASTE");
            csvImportService.importNAMASTECsv(file);
            saveImportHistory(file, systemName, "SUCCESS", reportDescription, auth);
            return ResponseEntity.ok(Map.of("message", "Import successful"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/import/history")
    public ResponseEntity<?> getImportHistory(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int limit) {
        Page<ImportHistory> history = importHistoryRepository
                .findByIsDeletedFalseOrderByImportTimeDesc(PageRequest.of(page - 1, limit));
        return ResponseEntity.ok(Map.of(
                "records", history.getContent(),
                "total", history.getTotalElements(),
                "page", page
        ));
    }

    @DeleteMapping("/import/history/{id}")
    public ResponseEntity<?> deleteImportHistory(@PathVariable Long id) {
        ImportHistory h = importHistoryRepository.findById(id).orElse(null);
        if (h == null) return ResponseEntity.status(404).body("Not found");
        h.setIsDeleted(true);
        h.setDeletedAt(LocalDateTime.now());
        importHistoryRepository.save(h);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    @GetMapping("/import/history/{id}/download")
    public ResponseEntity<byte[]> downloadImportHistoryFile(@PathVariable Long id) {
        ImportHistory h = importHistoryRepository.findById(id).orElse(null);
        if (h == null || h.getFileContent() == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + h.getFileName())
                .contentType(MediaType.parseMediaType(h.getFileContentType()))
                .body(h.getFileContent());
    }

    // --- Code Systems ---

    @GetMapping("/code-systems")
    public ResponseEntity<?> getCodeSystems() {
        return ResponseEntity.ok(codeSystemRepository.findAll());
    }

    @PostMapping("/code-systems")
    public ResponseEntity<?> createCodeSystem(@RequestBody Map<String, Object> data) {
        try {
            CodeSystem cs = CodeSystem.builder()
                    .name((String) data.get("name"))
                    .displayName((String) data.get("displayName"))
                    .description((String) data.get("description"))
                    .version((String) data.get("version"))
                    .type((String) data.getOrDefault("type", "LOCAL"))
                    .sourceUrl((String) data.get("sourceUrl"))
                    .isActive(true)
                    .build();
            codeSystemRepository.save(cs);
            return ResponseEntity.ok(cs);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/code-systems/{id}")
    public ResponseEntity<?> updateCodeSystem(@PathVariable Long id,
                                               @RequestBody Map<String, Object> data) {
        CodeSystem cs = codeSystemRepository.findById(id).orElse(null);
        if (cs == null) return ResponseEntity.status(404).body("Not found");

        if (data.containsKey("displayName")) cs.setDisplayName((String) data.get("displayName"));
        if (data.containsKey("description")) cs.setDescription((String) data.get("description"));
        if (data.containsKey("version")) cs.setVersion((String) data.get("version"));
        if (data.containsKey("sourceUrl")) cs.setSourceUrl((String) data.get("sourceUrl"));
        if (data.containsKey("type")) cs.setType((String) data.get("type"));

        codeSystemRepository.save(cs);
        return ResponseEntity.ok(cs);
    }

    @PutMapping("/code-systems/{id}/active")
    public ResponseEntity<?> setCodeSystemActive(@PathVariable Long id,
                                                  @RequestParam boolean active) {
        CodeSystem cs = codeSystemRepository.findById(id).orElse(null);
        if (cs == null) return ResponseEntity.status(404).body("Not found");
        cs.setIsActive(active);
        codeSystemRepository.save(cs);
        return ResponseEntity.ok(cs);
    }

    @GetMapping("/code-systems/{id}/versions")
    public ResponseEntity<?> getCodeSystemVersions(@PathVariable Long id) {
        return ResponseEntity.ok(systemVersionRepository.findBySystemIdOrderByImportedAtDesc(id));
    }

    // --- Admin Mappings ---

    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings() {
        List<Mapping> all = mappingRepository.findAll();
        List<Map<String, Object>> result = all.stream().map(this::buildMappingMap).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/mappings")
    public ResponseEntity<?> addMapping(@RequestBody Map<String, Object> data,
                                         Authentication auth) {
        try {
            Long sourceId = Long.valueOf(data.get("sourceConceptId").toString());
            Long targetId = Long.valueOf(data.get("targetConceptId").toString());

            Concept source = conceptRepository.findById(sourceId)
                    .orElseThrow(() -> new RuntimeException("Source not found"));
            Concept target = conceptRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Target not found"));

            Mapping mapping = Mapping.builder()
                    .fromConcept(source)
                    .toConcept(target)
                    .mappingType((String) data.getOrDefault("mappingType", "EQUIVALENT"))
                    .confidence(data.get("confidence") != null ?
                            ((Number) data.get("confidence")).doubleValue() : null)
                    .status("VERIFIED")
                    .createdBy(auth != null ? auth.getName() : null)
                    .build();

            mappingRepository.save(mapping);
            return ResponseEntity.ok(Map.of("message", "Mapping added"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PatchMapping("/mappings/{id}/verify")
    public ResponseEntity<?> verifyMapping(@PathVariable Long id, Authentication auth) {
        Mapping m = mappingRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.status(404).body("Not found");
        m.setStatus("VERIFIED");
        m.setVerifiedBy(auth != null ? auth.getName() : null);
        m.setVerifiedAt(LocalDateTime.now());
        mappingRepository.save(m);
        return ResponseEntity.ok(Map.of("message", "Mapping verified"));
    }

    @PatchMapping("/mappings/{id}/reject")
    public ResponseEntity<?> rejectMapping(@PathVariable Long id,
                                            @RequestBody(required = false) Map<String, String> data) {
        Mapping m = mappingRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.status(404).body("Not found");
        m.setStatus("REJECTED");
        if (data != null) m.setRejectionReason(data.get("reason"));
        mappingRepository.save(m);
        return ResponseEntity.ok(Map.of("message", "Mapping rejected"));
    }

    @PostMapping("/mappings/bulk-verify")
    public ResponseEntity<?> bulkVerifyMappings(@RequestBody Map<String, List<Long>> data,
                                                 Authentication auth) {
        List<Long> ids = data.get("ids");
        if (ids == null) return ResponseEntity.badRequest().body("No ids provided");
        ids.forEach(id -> {
            mappingRepository.findById(id).ifPresent(m -> {
                m.setStatus("VERIFIED");
                m.setVerifiedBy(auth != null ? auth.getName() : null);
                m.setVerifiedAt(LocalDateTime.now());
                mappingRepository.save(m);
            });
        });
        return ResponseEntity.ok(Map.of("message", "Bulk verified"));
    }

    @DeleteMapping("/mappings/{id}")
    public ResponseEntity<?> deleteMapping(@PathVariable Long id) {
        mappingRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Mapping deleted"));
    }

    // --- Concepts ---

    @PostMapping("/concepts/deactivate")
    public ResponseEntity<?> deactivateConcept(@RequestBody Map<String, String> data) {
        String code = data.get("code");
        String codeSystem = data.get("codeSystem");
        Optional<Concept> opt = conceptRepository.findByCodeAndSystem(code, codeSystem);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Not found");

        Concept c = opt.get();
        c.setIsActive(false);
        c.setLifecycleStatus("INACTIVE");
        c.setDeactivatedAt(LocalDateTime.now());
        conceptRepository.save(c);
        return ResponseEntity.ok(Map.of("message", "Concept deactivated"));
    }

    @GetMapping("/concepts/archived")
    public ResponseEntity<?> getArchivedConcepts() {
        List<Concept> archived = conceptRepository.findAll().stream()
                .filter(c -> !c.getIsActive() || "ARCHIVED".equals(c.getLifecycleStatus()))
                .toList();
        return ResponseEntity.ok(archived);
    }

    // --- Concept Requests ---

    @GetMapping("/concept-requests")
    public ResponseEntity<?> getConceptRequests() {
        return ResponseEntity.ok(conceptRequestRepository.findAllByOrderByCreatedAtDesc());
    }

    @PatchMapping("/concept-requests/{id}/approve")
    public ResponseEntity<?> approveConceptRequest(@PathVariable Long id, Authentication auth) {
        ConceptRequest cr = conceptRequestRepository.findById(id).orElse(null);
        if (cr == null) return ResponseEntity.status(404).body("Not found");
        cr.setStatus("APPROVED");
        cr.setProcessedBy(auth != null ? auth.getName() : null);
        cr.setProcessedAt(LocalDateTime.now());
        conceptRequestRepository.save(cr);
        return ResponseEntity.ok(Map.of("message", "Request approved"));
    }

    @PatchMapping("/concept-requests/{id}/reject")
    public ResponseEntity<?> rejectConceptRequest(@PathVariable Long id,
                                                   @RequestBody(required = false) Map<String, String> data) {
        ConceptRequest cr = conceptRequestRepository.findById(id).orElse(null);
        if (cr == null) return ResponseEntity.status(404).body("Not found");
        cr.setStatus("REJECTED");
        if (data != null) cr.setRejectionReason(data.getOrDefault("reason", ""));
        cr.setProcessedAt(LocalDateTime.now());
        conceptRequestRepository.save(cr);
        return ResponseEntity.ok(Map.of("message", "Request rejected"));
    }

    @PatchMapping("/concept-requests/bulk-reject")
    public ResponseEntity<?> bulkRejectConceptRequests(@RequestBody Map<String, Object> data) {
        List<Integer> ids = (List<Integer>) data.get("ids");
        String reason = (String) data.getOrDefault("reason", "");
        if (ids == null) return ResponseEntity.badRequest().body("No ids");
        ids.forEach(id -> {
            conceptRequestRepository.findById(id.longValue()).ifPresent(cr -> {
                cr.setStatus("REJECTED");
                cr.setRejectionReason(reason);
                cr.setProcessedAt(LocalDateTime.now());
                conceptRequestRepository.save(cr);
            });
        });
        return ResponseEntity.ok(Map.of("message", "Bulk rejected"));
    }

    // --- Dashboard ---

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboardStats() {
        return ResponseEntity.ok(Map.of(
                "totalConcepts", conceptRepository.count(),
                "totalMappings", mappingRepository.count(),
                "totalSystems", codeSystemRepository.count(),
                "pendingRequests", conceptRequestRepository.countByStatus("PENDING"),
                "pendingFeedback", mlFeedbackRepository.countByReviewed(false),
                "totalUsers", userRepository.count()
        ));
    }

    @GetMapping("/dashboard/imports")
    public ResponseEntity<?> getAdminDashboardImports() {
        return ResponseEntity.ok(Map.of(
                "totalImports", importHistoryRepository.countByIsDeletedFalse()
        ));
    }

    // --- ML Feedback ---

    @GetMapping("/ml-feedback")
    public ResponseEntity<?> getMlFeedback() {
        return ResponseEntity.ok(mlFeedbackRepository.findAllByOrderByCreatedAtDesc());
    }

    @PatchMapping("/ml-feedback/{id}/review")
    public ResponseEntity<?> markMlFeedbackReviewed(@PathVariable Long id,
                                                     @RequestBody(required = false) Map<String, String> data,
                                                     Authentication auth) {
        MlFeedback fb = mlFeedbackRepository.findById(id).orElse(null);
        if (fb == null) return ResponseEntity.status(404).body("Not found");
        fb.setReviewed(true);
        fb.setAdminDecision(data != null ? data.getOrDefault("decision", "ACCEPTED") : "ACCEPTED");
        fb.setReviewedBy(auth != null ? auth.getName() : null);
        fb.setReviewedAt(LocalDateTime.now());
        mlFeedbackRepository.save(fb);
        return ResponseEntity.ok(Map.of("message", "Feedback reviewed"));
    }

    @PatchMapping("/ml-feedback/bulk-review")
    public ResponseEntity<?> bulkReviewMlFeedback(@RequestBody Map<String, Object> data,
                                                    Authentication auth) {
        List<Integer> ids = (List<Integer>) data.get("ids");
        String decision = (String) data.getOrDefault("decision", "ACCEPTED");
        if (ids == null) return ResponseEntity.badRequest().body("No ids");
        ids.forEach(id -> {
            mlFeedbackRepository.findById(id.longValue()).ifPresent(fb -> {
                fb.setReviewed(true);
                fb.setAdminDecision(decision);
                fb.setReviewedBy(auth != null ? auth.getName() : null);
                fb.setReviewedAt(LocalDateTime.now());
                mlFeedbackRepository.save(fb);
            });
        });
        return ResponseEntity.ok(Map.of("message", "Bulk reviewed"));
    }

    @GetMapping("/ml-feedback/stats")
    public ResponseEntity<?> getMlFeedbackStats() {
        return ResponseEntity.ok(Map.of(
                "total", mlFeedbackRepository.count(),
                "pending", mlFeedbackRepository.countByReviewed(false),
                "reviewed", mlFeedbackRepository.countByReviewed(true),
                "confirmed", mlFeedbackRepository.countByFeedbackType("confirmed"),
                "corrected", mlFeedbackRepository.countByFeedbackType("corrected")
        ));
    }

    @GetMapping("/ml-feedback/export")
    public ResponseEntity<byte[]> exportMlFeedbackCsv() {
        List<MlFeedback> all = mlFeedbackRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder("id,query,predictedCode,correctCode,feedbackType,confidence,reviewed\n");
        for (MlFeedback fb : all) {
            csv.append(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    fb.getId(), fb.getQuery(), fb.getPredictedCode(),
                    fb.getCorrectCode(), fb.getFeedbackType(),
                    fb.getConfidence(), fb.getReviewed()));
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ml_feedback_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString().getBytes());
    }

    // --- Notifications ---

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(
                notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(auth.getName()));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n == null) return ResponseEntity.status(404).body("Not found");
        n.setStatus("READ");
        notificationRepository.save(n);
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    // --- Doctor Approval ---

    @PostMapping("/doctor/approve")
    public ResponseEntity<?> approveDoctor(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");
        user.setRegistrationStatus("APPROVED");
        user.setEnabled(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Doctor approved"));
    }

    @PostMapping("/doctor/reject")
    public ResponseEntity<?> rejectDoctor(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        AppUser user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(404).body("User not found");
        user.setRegistrationStatus("REJECTED");
        user.setRejectionReason(data.getOrDefault("reason", ""));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Doctor rejected"));
    }

    // --- Helpers ---

    private void saveImportHistory(MultipartFile file, String system, String status,
                                   String errorOrDesc, Authentication auth) {
        try {
            ImportHistory h = ImportHistory.builder()
                    .codeSystem(system)
                    .fileName(file.getOriginalFilename())
                    .fileContentType(file.getContentType())
                    .fileContent(file.getBytes())
                    .status(status)
                    .reportDescription("SUCCESS".equals(status) ? errorOrDesc : "")
                    .errorMessage("FAILED".equals(status) ? errorOrDesc : "")
                    .importedBy(auth != null ? auth.getName() : "unknown")
                    .build();
            importHistoryRepository.save(h);
        } catch (Exception ignored) {}
    }

    private Map<String, Object> buildMappingMap(Mapping m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("id", m.getFromConcept().getId());
        source.put("code", m.getFromConcept().getCode());
        source.put("displayName", m.getFromConcept().getDisplayName());
        source.put("codeSystem", m.getFromConcept().getCodeSystem().getName());
        map.put("sourceConcept", source);

        Map<String, Object> target = new LinkedHashMap<>();
        target.put("id", m.getToConcept().getId());
        target.put("code", m.getToConcept().getCode());
        target.put("displayName", m.getToConcept().getDisplayName());
        target.put("codeSystem", m.getToConcept().getCodeSystem().getName());
        map.put("targetConcept", target);

        map.put("mappingType", m.getMappingType());
        map.put("confidence", m.getConfidence());
        map.put("status", m.getStatus());
        map.put("createdBy", m.getCreatedBy());
        map.put("createdAt", m.getCreatedAt());
        return map;
    }
}
