package com.example.trip_split.dto;

import java.util.List;

public record TripSummaryResponse(Long tripId, String name,
                                  List<ExpenseResponse> expenses,
                                  List<BalanceResponse> balances,
                                  List<SettlementResponse> settlements) {}
