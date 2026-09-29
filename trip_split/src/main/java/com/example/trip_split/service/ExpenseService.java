package com.example.trip_split.service;

import java.util.List;
import com.example.trip_split.dto.*;

public interface ExpenseService {
    ExpenseResponse createExpense(Long tripId, ExpenseRequest request);
    List<ExpenseResponse> getExpensesByTrip(Long tripId);
    ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request);
    void deleteExpense(Long expenseId);
}

