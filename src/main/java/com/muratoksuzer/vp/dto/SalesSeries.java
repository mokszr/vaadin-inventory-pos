package com.muratoksuzer.vp.dto;

import java.util.List;

public record SalesSeries(
        List<String> labels,
        List<Number> values
) {}
