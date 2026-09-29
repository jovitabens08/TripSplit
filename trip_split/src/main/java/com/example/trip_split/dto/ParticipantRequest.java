package com.example.trip_split.dto;

import jakarta.validation.constraints.NotBlank;

public record ParticipantRequest(@NotBlank String name) {}