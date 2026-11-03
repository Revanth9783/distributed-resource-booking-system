package com.resourcebooking.booking.repository;

import com.resourcebooking.booking.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProcessedEventRepository
        extends JpaRepository<ProcessedEvent, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO processed_events (event_id, event_type)
            VALUES (:eventId, :eventType)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int markAsProcessed(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType
    );
}