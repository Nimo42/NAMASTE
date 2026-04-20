package com.ayush.terminology.namaste.service;


import com.ayush.terminology.namaste.dto.CodeDTO;
import com.ayush.terminology.namaste.dto.SearchResponseDTO;
import com.ayush.terminology.namaste.dto.SuggestionDTO;
import com.ayush.terminology.namaste.mappings.Mapping;
import com.ayush.terminology.namaste.model.Concept;
import com.ayush.terminology.namaste.repo.ConceptRepository;
import com.ayush.terminology.namaste.repo.MappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ConceptService {

    @Autowired
    private ConceptRepository conceptRepository;

    @Autowired
    private MappingRepository mappingRepository;

    /**
     * Search for concepts by displayName (disease name)
     * Returns unified suggestions with all code systems
     */
    public SearchResponseDTO search(String query, Integer limit) {
        limit = limit != null ? limit : 10; // default limit

        // Step 1: Find concepts by displayName
        List<Concept> matchedConcepts = conceptRepository
                .findByDisplayNameContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .toList();

        // Step 2: For each matched concept, build a suggestion with all codes
        List<SuggestionDTO> suggestions = matchedConcepts.stream()
                .map(this::buildSuggestionFromConcept)
                .toList();

        // Step 3: Return response
        SearchResponseDTO response = new SearchResponseDTO();
        response.setSuggestions(suggestions);
        response.setCount(suggestions.size());
        return response;
    }

    /**
     * Build a unified suggestion (with NAMASTE, TM2, BIOMED) from one concept
     */
    private SuggestionDTO buildSuggestionFromConcept(Concept concept) {
        SuggestionDTO suggestion = new SuggestionDTO();
        suggestion.setDisplayName(concept.getDisplayName());
        suggestion.setMatchedSystem(concept.getCodeSystem().getName());

        // Add the matched concept itself
        CodeDTO code = new CodeDTO(concept.getCode(), concept.getDisplayName());
        switch (concept.getCodeSystem().getName()) {
            case "NAMASTE" -> suggestion.setNamaste(code);
            case "ICD11_TM2" -> suggestion.setTm2(code);
            case "ICD11_BIOMED" -> suggestion.setBiomed(code);
        }

        // Find and add related concepts from mappings
        List<Mapping> mappings = mappingRepository.findByFromConceptId(concept.getId());
        for (Mapping mapping : mappings) {
            Concept targetConcept = mapping.getToConcept();
            CodeDTO targetCode = new CodeDTO(targetConcept.getCode(), targetConcept.getDisplayName());

            switch (targetConcept.getCodeSystem().getName()) {
                case "NAMASTE" -> suggestion.setNamaste(targetCode);
                case "ICD11_TM2" -> suggestion.setTm2(targetCode);
                case "ICD11_BIOMED" -> suggestion.setBiomed(targetCode);
            }
        }

        return suggestion;
    }
    /**
     * Translate from one code system to another
     */
    public SuggestionDTO translateCode(String code, String fromSystem, String toSystem) {
        // Find the source concept
        Optional<Concept> sourceConcept = conceptRepository
                .findByCodeAndSystem(code, fromSystem);

        if (sourceConcept.isEmpty()) {
            throw new RuntimeException("Code not found: " + code);
        }

        // Build suggestion with all related codes
        return buildSuggestionFromConcept(sourceConcept.get());
    }


    /**
     * Get a concept by code
     */
    public Optional<Concept> getConceptByCode(String code) {
        return conceptRepository.findByCode(code);
    }

    /**
     * Create a new concept
     */
    public Concept createConcept(Concept concept) {
        return conceptRepository.save(concept);
    }
}
