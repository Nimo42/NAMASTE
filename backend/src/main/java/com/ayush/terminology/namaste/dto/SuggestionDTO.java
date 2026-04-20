package com.ayush.terminology.namaste.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDTO {
    private String displayName;
    private String matchedSystem; // which system matched primarily
    private CodeDTO namaste;
    private CodeDTO tm2;
    private CodeDTO biomed;
    private Double confidenceScore; // null for normal search, present for AI
}
