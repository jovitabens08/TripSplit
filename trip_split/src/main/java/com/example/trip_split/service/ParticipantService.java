package com.example.trip_split.service;

import java.util.List;
import com.example.trip_split.dto.*;

public interface ParticipantService {
    ParticipantResponse addParticipant(Long tripId, ParticipantRequest request);
    List<ParticipantResponse> getParticipants(Long tripId);
}
