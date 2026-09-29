package com.example.trip_split.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trip_split.model.Trip;

public interface TripRepository extends JpaRepository<Trip, Long> {
    
}