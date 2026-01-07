package com.muratoksuzer.vp.dto;

import java.util.List;

public record TopProductsSeries(
        List<String> labels,
        List<Number> values
) {}
