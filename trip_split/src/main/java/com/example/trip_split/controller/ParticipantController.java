package com.example.trip_split.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.trip_split.dto.*;
import com.example.trip_split.service.ParticipantService;

@RestController
@RequestMapping("/trips/{tripId}/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @PostMapping
    public ResponseEntity<ParticipantResponse> addParticipant(
            @PathVariable Long tripId, @Valid @RequestBody ParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(participantService.addParticipant(tripId, request));
    }

    @GetMapping
    public List<ParticipantResponse> getParticipants(@PathVariable Long tripId) {
        return participantService.getParticipants(tripId);
    }
}