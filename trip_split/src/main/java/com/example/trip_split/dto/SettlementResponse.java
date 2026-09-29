package com.example.trip_split.dto;

import java.math.BigDecimal;

public record SettlementResponse(Long fromId, String fromName,
                                 Long toId, String toName, BigDecimal amount) {}
