package com.example.trip_split.service;

import java.util.List;
import com.example.trip_split.dto.*;

public interface TripService {
    TripResponse createTrip(TripRequest request);
    TripResponse getTripById(Long id);
    List<TripResponse> getAllTrips();
    void deleteTrip(Long id);
    TripSummaryResponse getSummary(Long id);
}

