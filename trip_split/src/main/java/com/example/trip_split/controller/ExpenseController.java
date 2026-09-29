package com.example.trip_split.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.example.trip_split.dto.*;
import com.example.trip_split.service.ExpenseService;

@RestController
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable Long tripId, @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(expenseService.createExpense(tripId, request));
    }

    @GetMapping("/trips/{tripId}/expenses")
    public List<ExpenseResponse> getExpenses(@PathVariable Long tripId) {
        return expenseService.getExpensesByTrip(tripId);
    }

    @PutMapping("/expenses/{expenseId}")
    public ExpenseResponse updateExpense(
            @PathVariable Long expenseId, @Valid @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(expenseId, request);
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}