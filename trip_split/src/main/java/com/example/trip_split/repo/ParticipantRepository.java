package com.example.trip_split.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.trip_split.model.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    
}