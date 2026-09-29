package com.example.trip_split.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.trip_split.dto.*;
import com.example.trip_split.service.SettlementService;

@RestController
@RequestMapping("/trips/{tripId}")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping("/balances")
    public List<BalanceResponse> getBalances(@PathVariable Long tripId) {
        return settlementService.getBalances(tripId);
    }

    @PostMapping("/settlements")
    public ResponseEntity<List<SettlementResponse>> generateSettlements(@PathVariable Long tripId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(settlementService.generateSettlements(tripId));
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements(@PathVariable Long tripId) {
        return settlementService.getSettlements(tripId);
    }
}