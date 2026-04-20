package com.ayush.terminology.namaste.dto;

import lombok.Getter;
import lombok.Setter;

public class AiSearchRequest {

    @Getter
    @Setter
    private String text;

    @Getter
    @Setter
    private boolean aiEnabled;

    @Getter
    @Setter
    private Double minConfidence;
}
