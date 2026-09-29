package com.example.trip_split.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.trip_split.dto.*;
import com.example.trip_split.exception.ResourceNotFoundException;
import com.example.trip_split.model.Expense;
import com.example.trip_split.model.Participant;
import com.example.trip_split.model.Settlement;
import com.example.trip_split.model.Trip;
import com.example.trip_split.repo.ExpenseRepository;
import com.example.trip_split.repo.SettlementRepository;
import com.example.trip_split.repo.TripRepository;

@Service
public class SettlementServiceImpl implements SettlementService {

    private final TripRepository tripRepo;
    private final ExpenseRepository expenseRepo;
    private final SettlementRepository settlementRepo;

    public SettlementServiceImpl(TripRepository tripRepo, ExpenseRepository expenseRepo,
                                 SettlementRepository settlementRepo) {
        this.tripRepo = tripRepo;
        this.expenseRepo = expenseRepo;
        this.settlementRepo = settlementRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceResponse> getBalances(Long tripId) {
        Trip trip = findTrip(tripId);
        Map<Long, BigDecimal> balances = computeBalances(trip, expenseRepo.findByTripId(tripId));
        return trip.getParticipants().stream()
            .map(p -> new BalanceResponse(p.getId(), p.getName(), balances.get(p.getId())))
            .toList();
    }

    @Override
    @Transactional
    public List<SettlementResponse> generateSettlements(Long tripId) {
        Trip trip = findTrip(tripId);
        Map<Long, BigDecimal> balances = computeBalances(trip, expenseRepo.findByTripId(tripId));
        Map<Long, Participant> people = trip.getParticipants().stream()
            .collect(Collectors.toMap(Participant::getId, p -> p));

        settlementRepo.deleteByTripId(tripId);

        List<Settlement> settlements = new ArrayList<>();
        while (true) {
            Long debtorId = null;
            Long creditorId = null;
            for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
                BigDecimal b = entry.getValue();
                if (b.signum() < 0 && (debtorId == null || b.compareTo(balances.get(debtorId)) < 0))
                    debtorId = entry.getKey();
                if (b.signum() > 0 && (creditorId == null || b.compareTo(balances.get(creditorId)) > 0))
                    creditorId = entry.getKey();
            }
            if (debtorId == null || creditorId == null)
                break;

            BigDecimal amount = balances.get(debtorId).negate().min(balances.get(creditorId));

            Settlement s = new Settlement();
            s.setTrip(trip);
            s.setFromParticipant(people.get(debtorId));
            s.setToParticipant(people.get(creditorId));
            s.setAmount(amount);
            settlements.add(s);

            balances.merge(debtorId, amount, BigDecimal::add);
            balances.merge(creditorId, amount.negate(), BigDecimal::add);
        }

        boolean cleared = balances.values().stream().allMatch(b -> b.signum() == 0);
        if (!cleared)
            throw new IllegalStateException("Settlement did not clear all balances for trip " + tripId);

        return settlementRepo.saveAll(settlements).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(Long tripId) {
        findTrip(tripId);
        return settlementRepo.findByTripId(tripId).stream().map(this::toResponse).toList();
    }

    private Map<Long, BigDecimal> computeBalances(Trip trip, List<Expense> expenses) {
        Map<Long, BigDecimal> balances = new LinkedHashMap<>();
        for (Participant p : trip.getParticipants()) {
            balances.put(p.getId(), BigDecimal.ZERO);
        }

        for (Expense e : expenses) {
            balances.merge(e.getPaidBy().getId(), e.getAmount(), BigDecimal::add);

            List<Participant> sharers = e.getSharedBy().stream()
                .sorted(Comparator.comparing(Participant::getId))
                .toList();
            int n = sharers.size();
            BigDecimal count = BigDecimal.valueOf(n);

            BigDecimal base = e.getAmount().divide(count, 2, RoundingMode.DOWN);
            BigDecimal remainder = e.getAmount().subtract(base.multiply(count));
            int extraPaise = remainder.movePointRight(2).intValueExact();
            BigDecimal onePaisa = new BigDecimal("0.01");

            for (int i = 0; i < n; i++) {
                BigDecimal share = (i < extraPaise) ? base.add(onePaisa) : base;
                balances.merge(sharers.get(i).getId(), share.negate(), BigDecimal::add);
            }
        }

        BigDecimal total = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.signum() != 0)
            throw new IllegalStateException("Balances do not sum to zero for trip " + trip.getId());

        return balances;
    }

    private Trip findTrip(Long id) {
        return tripRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Trip " + id + " not found"));
    }

    private SettlementResponse toResponse(Settlement s) {
        return new SettlementResponse(
            s.getFromParticipant().getId(), s.getFromParticipant().getName(),
            s.getToParticipant().getId(), s.getToParticipant().getName(),
            s.getAmount());
    }
}