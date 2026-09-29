package com.example.trip_split.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.trip_split.dto.*;
import com.example.trip_split.exception.ResourceNotFoundException;
import com.example.trip_split.model.Participant;
import com.example.trip_split.model.Trip;
import com.example.trip_split.repo.ParticipantRepository;
import com.example.trip_split.repo.TripRepository;

@Service
public class ParticipantServiceImpl implements ParticipantService {

    private final TripRepository tripRepo;
    private final ParticipantRepository participantRepo;

    public ParticipantServiceImpl(TripRepository tripRepo, ParticipantRepository participantRepo) {
        this.tripRepo = tripRepo;
        this.participantRepo = participantRepo;
    }

    @Override
    @Transactional
    public ParticipantResponse addParticipant(Long tripId, ParticipantRequest request) {
        Trip trip = findTrip(tripId);
        Participant p = new Participant();
        p.setName(request.name());
        p.setTrip(trip);
        Participant saved = participantRepo.save(p);
        return new ParticipantResponse(saved.getId(), saved.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipantResponse> getParticipants(Long tripId) {
        return findTrip(tripId).getParticipants().stream()
            .map(p -> new ParticipantResponse(p.getId(), p.getName()))
            .toList();
    }

    private Trip findTrip(Long id) {
        return tripRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip " + id + " not found"));
    }
}