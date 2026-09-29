package com.example.trip_split.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TripResponse(Long id, String name, LocalDateTime createdAt,
                           List<ParticipantResponse> participants) {}
