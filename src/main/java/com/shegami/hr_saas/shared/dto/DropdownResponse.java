package com.shegami.hr_saas.shared.dto;

import java.util.List;

public record DropdownResponse<T>(List<T> items, long total) {}
