package com.example.trip_split.service;

import java.util.List;
import com.example.trip_split.dto.*;

public interface SettlementService {
    List<BalanceResponse> getBalances(Long tripId);
    List<SettlementResponse> generateSettlements(Long tripId);
    List<SettlementResponse> getSettlements(Long tripId);
}

