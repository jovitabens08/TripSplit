package com.example.trip_split.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.trip_split.dto.*;
import com.example.trip_split.exception.ResourceNotFoundException;
import com.example.trip_split.model.Participant;
import com.example.trip_split.model.Trip;
import com.example.trip_split.repo.ExpenseRepository;
import com.example.trip_split.repo.SettlementRepository;
import com.example.trip_split.repo.TripRepository;

@Service
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepo;
    private final ExpenseRepository expenseRepo;
    private final SettlementRepository settlementRepo;
    private final ExpenseService expenseService;
    private final SettlementService settlementService;

    public TripServiceImpl(TripRepository tripRepo, ExpenseRepository expenseRepo,
                           SettlementRepository settlementRepo,
                           ExpenseService expenseService,
                           SettlementService settlementService) {
        this.tripRepo = tripRepo;
        this.expenseRepo = expenseRepo;
        this.settlementRepo = settlementRepo;
        this.expenseService = expenseService;
        this.settlementService = settlementService;
    }

    @Override
    @Transactional
    public TripResponse createTrip(TripRequest request) {
        Trip trip = new Trip();
        trip.setName(request.name());
        for (String name : request.participantNames()) {
            Participant p = new Participant();
            p.setName(name);
            p.setTrip(trip);
            trip.getParticipants().add(p);
        }
        return toResponse(tripRepo.save(trip));
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        return toResponse(findTrip(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {
        return tripRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteTrip(Long id) {
        Trip trip = findTrip(id);
        settlementRepo.deleteByTripId(id);
        expenseRepo.deleteAll(expenseRepo.findByTripId(id));
        expenseRepo.flush();
        tripRepo.delete(trip);
    }

    @Override
    public TripSummaryResponse getSummary(Long id) {
        Trip trip = findTrip(id);
        return new TripSummaryResponse(
            trip.getId(), trip.getName(),
            expenseService.getExpensesByTrip(id),
            settlementService.getBalances(id),
            settlementService.generateSettlements(id));
    }

    private Trip findTrip(Long id) {
        return tripRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip " + id + " not found"));
    }

    private TripResponse toResponse(Trip t) {
        List<ParticipantResponse> people = t.getParticipants().stream()
            .map(p -> new ParticipantResponse(p.getId(), p.getName()))
            .toList();
        return new TripResponse(t.getId(), t.getName(), t.getCreatedAt(), people);
    }
}
