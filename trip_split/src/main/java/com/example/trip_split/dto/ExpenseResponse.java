package com.example.trip_split.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseResponse(Long id, String description, BigDecimal amount,
                              Long paidById, String paidByName,
                              List<String> sharedByNames) {}
