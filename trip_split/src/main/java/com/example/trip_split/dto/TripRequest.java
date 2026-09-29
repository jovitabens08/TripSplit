package com.example.trip_split.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record TripRequest(
    @NotBlank String name,
    @NotEmpty List<@NotBlank String> participantNames) {}
