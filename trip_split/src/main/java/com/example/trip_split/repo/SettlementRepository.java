package com.example.trip_split.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trip_split.model.Settlement;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByTripId(Long tripId);
    void deleteByTripId(Long tripId);
}