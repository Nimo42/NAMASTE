package com.ayush.terminology.namaste.controller;

import com.ayush.terminology.namaste.dto.AiSearchRequest;
import com.ayush.terminology.namaste.dto.SearchResponseDTO;
import com.ayush.terminology.namaste.dto.SuggestionDTO;
import com.ayush.terminology.namaste.model.CodeSystem;
import com.ayush.terminology.namaste.model.Concept;
import com.ayush.terminology.namaste.model.ConceptRequest;
import com.ayush.terminology.namaste.model.MlFeedback;
import com.ayush.terminology.namaste.mappings.Mapping;
import com.ayush.terminology.namaste.repo.*;
import com.ayush.terminology.namaste.service.AuditService;
import com.ayush.terminology.namaste.service.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/codes")
@CrossOrigin(origins = "*")
public class CodeSearchController {

    @Autowired private ConceptService conceptService;
    @Autowired private AuditService auditService;
    @Autowired private RestTemplate restTemplate;
    @Autowired private MlFeedbackRepository mlFeedbackRepository;
    @Autowired private ConceptRequestRepository conceptRequestRepository;
    @Autowired private ConceptRepository conceptRepository;
    @Autowired private CodeSystemRepository codeSystemRepository;
    @Autowired private MappingRepository mappingRepository;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    // GET /api/v1/codes/search
    @GetMapping("/search")
    public ResponseEntity<SearchResponseDTO> searchCodes(
            @RequestParam(name = "q") String query,
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "searchWithin", required = false, defaultValue = "ALL") String searchWithin,
            @RequestParam(name = "onlyActive", required = false, defaultValue = "false") Boolean onlyActive,
            @RequestParam(name = "minConfidence", required = false) Double minConfidence) {
        try {
            auditService.logAction(null, null, "SEARCH", "/codes/search", "Query: " + query);
            SearchResponseDTO response = conceptService.search(query, limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // GET /api/v1/codes/translate
    @GetMapping("/translate")
    public ResponseEntity<SuggestionDTO> translate(
            @RequestParam(name = "code") String code,
            @RequestParam(name = "from") String fromSystem) {
        try {
            auditService.logAction(null, null, "TRANSLATE", "/codes/translate", code + " from " + fromSystem);
            SuggestionDTO result = conceptService.translateCode(code, fromSystem, "ALL");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // POST /api/v1/codes/ai-search
    @PostMapping("/ai-search")
    public ResponseEntity<SuggestionDTO> aiSearch(@RequestBody AiSearchRequest request) {
        try {
            if (!request.isAiEnabled()) return ResponseEntity.badRequest().build();

            auditService.logAction(null, null, "AI_SEARCH", "/codes/ai-search", request.getText());

            Map<String, String> body = Map.of("text", request.getText());
            ResponseEntity<Map> response = restTemplate.postForEntity(mlServiceUrl, body, Map.class);
            Map<String, Object> result = response.getBody();
            if (result == null) return ResponseEntity.status(500).build();

            String predictedCode = (String) result.get("predicted_code");
            Number confidenceNumber = (Number) result.get("confidence");
            if (predictedCode == null || confidenceNumber == null) return ResponseEntity.status(500).build();

            SuggestionDTO mapped = conceptService.translateCode(predictedCode, "NAMASTE", "ALL");
            if (mapped == null) return ResponseEntity.status(404).build();

            mapped.setConfidenceScore(confidenceNumber.doubleValue());
            return ResponseEntity.ok(mapped);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // POST /api/v1/codes/ml-feedback
    @PostMapping("/ml-feedback")
    public ResponseEntity<?> submitMlFeedback(@RequestBody Map<String, Object> data,
                                              Authentication auth) {
        try {
            MlFeedback feedback = MlFeedback.builder()
                    .query((String) data.get("query"))
                    .predictedCode((String) data.get("predictedCode"))
                    .predictedSystem((String) data.getOrDefault("predictedSystem", "NAMASTE"))
                    .correctCode((String) data.getOrDefault("correctCode", ""))
                    .correctSystem((String) data.getOrDefault("correctSystem", ""))
                    .feedbackType((String) data.getOrDefault("feedbackType", "confirmed"))
                    .confidence(data.get("confidence") != null ? ((Number) data.get("confidence")).doubleValue() : null)
                    .userId(auth != null ? auth.getName() : null)
                    .build();

            mlFeedbackRepository.save(feedback);
            return ResponseEntity.ok(Map.of("message", "Feedback submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // POST /api/v1/codes/concept-requests
    @PostMapping("/concept-requests")
    public ResponseEntity<?> requestConceptAddition(@RequestBody Map<String, String> data,
                                                     Authentication auth) {
        try {
            ConceptRequest request = ConceptRequest.builder()
                    .term(data.get("term"))
                    .description(data.get("description"))
                    .suggestedCode(data.get("suggestedCode"))
                    .suggestedSystem(data.get("suggestedSystem"))
                    .reason(data.getOrDefault("reason", ""))
                    .requestedBy(auth != null ? auth.getName() : "anonymous")
                    .build();

            conceptRequestRepository.save(request);
            return ResponseEntity.ok(Map.of("message", "Concept request submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // GET /api/v1/codes/concept-requests/mine
    @GetMapping("/concept-requests/mine")
    public ResponseEntity<?> getMyConceptRequests(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        List<ConceptRequest> requests = conceptRequestRepository
                .findByRequestedByOrderByCreatedAtDesc(auth.getName());
        return ResponseEntity.ok(requests);
    }

    // GET /api/v1/codes/ml-train-status
    @GetMapping("/ml-train-status")
    public ResponseEntity<?> mlTrainStatus() {
        return ResponseEntity.ok(Map.of("status", "idle", "lastTrained", "N/A"));
    }

    // GET /api/v1/codes/stats
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        long totalConcepts = conceptRepository.count();
        long totalMappings = mappingRepository.count();
        long totalSystems = codeSystemRepository.count();
        return ResponseEntity.ok(Map.of(
                "totalConcepts", totalConcepts,
                "totalMappings", totalMappings,
                "totalSystems", totalSystems
        ));
    }

    // GET /api/v1/codes/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDoctorDashboardStats(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String email = auth.getName();

        long myMappings = mappingRepository.findAll().stream()
                .filter(m -> email.equals(m.getCreatedBy())).count();
        long myRequests = conceptRequestRepository
                .findByRequestedByOrderByCreatedAtDesc(email).size();

        return ResponseEntity.ok(Map.of(
                "myMappings", myMappings,
                "myConceptRequests", myRequests,
                "totalConcepts", conceptRepository.count(),
                "totalSystems", codeSystemRepository.count()
        ));
    }

    // GET /api/v1/codes/concepts/detail
    @GetMapping("/concepts/detail")
    public ResponseEntity<?> conceptDetail(@RequestParam String code,
                                           @RequestParam String system) {
        try {
            Optional<Concept> concept = conceptRepository.findByCodeAndSystem(code, system);
            if (concept.isEmpty()) return ResponseEntity.status(404).body("Concept not found");

            Concept c = concept.get();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("id", c.getId());
            result.put("code", c.getCode());
            result.put("displayName", c.getDisplayName());
            result.put("description", c.getDescription());
            result.put("codeSystem", c.getCodeSystem().getName());
            result.put("isActive", c.getIsActive());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // GET /api/v1/codes/resolve
    @GetMapping("/resolve")
    public ResponseEntity<?> resolveCode(@RequestParam String code,
                                         @RequestParam(required = false) String system) {
        try {
            if (system != null) {
                Optional<Concept> concept = conceptRepository.findByCodeAndSystem(code, system);
                return concept.map(c -> ResponseEntity.ok((Object) buildConceptMap(c)))
                        .orElse(ResponseEntity.status(404).body("Not found"));
            }
            Optional<Concept> concept = conceptRepository.findByCode(code);
            return concept.map(c -> ResponseEntity.ok((Object) buildConceptMap(c)))
                    .orElse(ResponseEntity.status(404).body("Not found"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // GET /api/v1/codes/systems
    @GetMapping("/systems")
    public ResponseEntity<?> getPublicSystems() {
        List<CodeSystem> systems = codeSystemRepository.findAll();
        return ResponseEntity.ok(systems);
    }

    // GET /api/v1/codes/concepts/{id}
    @GetMapping("/concepts/{id}")
    public ResponseEntity<?> getConceptById(@PathVariable Long id) {
        Optional<Concept> concept = conceptRepository.findById(id);
        return concept.map(c -> ResponseEntity.ok((Object) buildConceptMap(c)))
                .orElse(ResponseEntity.status(404).body("Not found"));
    }

    // GET /api/v1/codes/mappings (doctor mappings)
    @GetMapping("/mappings")
    public ResponseEntity<?> getMappings(Authentication auth,
                                         @RequestParam(required = false, defaultValue = "") String q,
                                         @RequestParam(required = false, defaultValue = "1") int page) {
        if (auth == null) return ResponseEntity.status(401).build();

        List<Mapping> all = mappingRepository.findAll();
        String email = auth.getName();

        List<Map<String, Object>> mappings = all.stream()
                .filter(m -> email.equals(m.getCreatedBy()))
                .map(this::buildMappingMap)
                .toList();

        int pageSize = 20;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, mappings.size());
        List<Map<String, Object>> paged = start < mappings.size() ?
                mappings.subList(start, end) : List.of();

        return ResponseEntity.ok(Map.of(
                "mappings", paged,
                "total", mappings.size(),
                "page", page
        ));
    }

    // POST /api/v1/codes/mappings (doctor add mapping)
    @PostMapping("/mappings")
    public ResponseEntity<?> addDoctorMapping(@RequestBody Map<String, Object> data,
                                               Authentication auth) {
        try {
            Long sourceId = Long.valueOf(data.get("sourceConceptId").toString());
            Long targetId = Long.valueOf(data.get("targetConceptId").toString());

            Concept source = conceptRepository.findById(sourceId)
                    .orElseThrow(() -> new RuntimeException("Source concept not found"));
            Concept target = conceptRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Target concept not found"));

            Mapping mapping = Mapping.builder()
                    .fromConcept(source)
                    .toConcept(target)
                    .mappingType((String) data.getOrDefault("mappingType", "EQUIVALENT"))
                    .confidence(data.get("confidence") != null ?
                            ((Number) data.get("confidence")).doubleValue() : null)
                    .status("PENDING")
                    .createdBy(auth != null ? auth.getName() : null)
                    .build();

            mappingRepository.save(mapping);
            return ResponseEntity.ok(Map.of("message", "Mapping added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // PATCH /api/v1/codes/concepts/{id}/deactivate
    @PatchMapping("/concepts/{id}/deactivate")
    public ResponseEntity<?> deactivateDoctorConcept(@PathVariable Long id,
                                                      @RequestBody Map<String, String> data) {
        try {
            Concept concept = conceptRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Concept not found"));
            concept.setIsActive(false);
            concept.setLifecycleStatus("INACTIVE");
            concept.setDeactivatedAt(LocalDateTime.now());
            conceptRepository.save(concept);
            return ResponseEntity.ok(Map.of("message", "Concept deactivated"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // PATCH /api/v1/codes/concepts/{id}/reactivate
    @PatchMapping("/concepts/{id}/reactivate")
    public ResponseEntity<?> reactivateDoctorConcept(@PathVariable Long id) {
        try {
            Concept concept = conceptRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Concept not found"));
            concept.setIsActive(true);
            concept.setLifecycleStatus("ACTIVE");
            concept.setDeactivatedAt(null);
            conceptRepository.save(concept);
            return ResponseEntity.ok(Map.of("message", "Concept reactivated"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    // Helper methods
    private Map<String, Object> buildConceptMap(Concept c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("code", c.getCode());
        map.put("displayName", c.getDisplayName());
        map.put("description", c.getDescription());
        map.put("codeSystem", c.getCodeSystem().getName());
        map.put("isActive", c.getIsActive());
        return map;
    }

    private Map<String, Object> buildMappingMap(Mapping m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("sourceConcept", buildConceptMap(m.getFromConcept()));
        map.put("targetConcept", buildConceptMap(m.getToConcept()));
        map.put("mappingType", m.getMappingType());
        map.put("confidence", m.getConfidence());
        map.put("status", m.getStatus());
        map.put("createdAt", m.getCreatedAt());
        return map;
    }
}