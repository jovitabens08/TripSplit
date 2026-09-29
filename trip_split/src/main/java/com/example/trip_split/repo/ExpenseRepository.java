package com.example.trip_split.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trip_split.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTripId(Long tripId);
}