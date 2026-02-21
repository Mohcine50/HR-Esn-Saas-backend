package com.shegami.hr_saas.shared.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DropdownSearchRequest {

    private String search = "";

    @Min(1)
    @Max(100)
    private int limit = 10;
}