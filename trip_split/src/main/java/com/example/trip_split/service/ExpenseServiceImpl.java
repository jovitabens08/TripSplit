package com.example.trip_split.service;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.trip_split.dto.*;
import com.example.trip_split.exception.BadRequestException;
import com.example.trip_split.exception.ResourceNotFoundException;
import com.example.trip_split.model.Expense;
import com.example.trip_split.model.Participant;
import com.example.trip_split.model.Trip;
import com.example.trip_split.repo.ExpenseRepository;
import com.example.trip_split.repo.ParticipantRepository;
import com.example.trip_split.repo.SettlementRepository;
import com.example.trip_split.repo.TripRepository;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final TripRepository tripRepo;
    private final ParticipantRepository participantRepo;
    private final ExpenseRepository expenseRepo;
    private final SettlementRepository settlementRepo;

    public ExpenseServiceImpl(TripRepository tripRepo, ParticipantRepository participantRepo,
                              ExpenseRepository expenseRepo, SettlementRepository settlementRepo) {
        this.tripRepo = tripRepo;
        this.participantRepo = participantRepo;
        this.expenseRepo = expenseRepo;
        this.settlementRepo = settlementRepo;
    }

    @Override
    @Transactional
    public ExpenseResponse createExpense(Long tripId, ExpenseRequest request) {
        Trip trip = tripRepo.findById(tripId)
            .orElseThrow(() -> new ResourceNotFoundException("Trip " + tripId + " not found"));

        Expense expense = new Expense();
        expense.setTrip(trip);
        applyRequest(expense, request, tripId);

        settlementRepo.deleteByTripId(tripId);
        return toResponse(expenseRepo.save(expense));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByTrip(Long tripId) {
        if (!tripRepo.existsById(tripId))
            throw new ResourceNotFoundException("Trip " + tripId + " not found");
        return expenseRepo.findByTripId(tripId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request) {
        Expense expense = findExpense(expenseId);
        Long tripId = expense.getTrip().getId();

        applyRequest(expense, request, tripId);

        settlementRepo.deleteByTripId(tripId);
        return toResponse(expenseRepo.save(expense));
    }

    @Override
    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = findExpense(expenseId);
        Long tripId = expense.getTrip().getId();
        expenseRepo.delete(expense);
        settlementRepo.deleteByTripId(tripId);
    }

    private void applyRequest(Expense expense, ExpenseRequest request, Long tripId) {
        List<Long> sharerIds = request.sharedByIds().stream().distinct().toList();
        if (sharerIds.isEmpty())
            throw new BadRequestException("An expense must be shared by at least one participant");

        expense.setDescription(request.description());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setPaidBy(findParticipantInTrip(request.paidById(), tripId));

        List<Participant> sharers = new ArrayList<>();
        for (Long id : sharerIds) {
            sharers.add(findParticipantInTrip(id, tripId));
        }
        expense.setSharedBy(sharers);
    }

    private Participant findParticipantInTrip(Long participantId, Long tripId) {
        Participant p = participantRepo.findById(participantId)
            .orElseThrow(() -> new ResourceNotFoundException("Participant " + participantId + " not found"));
        if (!p.getTrip().getId().equals(tripId))
            throw new BadRequestException("Participant " + participantId + " does not belong to trip " + tripId);
        return p;
    }

    private Expense findExpense(Long id) {
        return expenseRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense " + id + " not found"));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
            e.getId(), e.getDescription(), e.getAmount(),
            e.getPaidBy().getId(), e.getPaidBy().getName(),
            e.getSharedBy().stream().map(Participant::getName).toList());
    }
}
