package com.example.trip_split.dto;

import java.math.BigDecimal;

public record BalanceResponse(Long participantId, String name, BigDecimal balance) {}
