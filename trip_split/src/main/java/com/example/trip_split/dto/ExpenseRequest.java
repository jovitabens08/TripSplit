package com.example.trip_split.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpenseRequest(
    @NotBlank String description,
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal amount,
    @NotNull Long paidById,
    @NotEmpty List<@NotNull Long> sharedByIds) {}