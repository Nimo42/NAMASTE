package com.ayush.terminology.namaste.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private List<SuggestionDTO> suggestions;
    private Integer count;
}
