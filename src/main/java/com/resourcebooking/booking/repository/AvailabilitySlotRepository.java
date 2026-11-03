
package com.resourcebooking.booking.repository;

import com.resourcebooking.booking.entity.AvailabilitySlot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvailabilitySlotRepository
        extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByResourceId(Long resourceId);

    // Find the resource associated with a slot.
    @Query("""
        SELECT s.resourceId
        FROM AvailabilitySlot s
        WHERE s.id = :slotId
        """)
    Long findResourceIdBySlotId(@Param("slotId") Long slotId);

    // Reserve capacity atomically when a booking is created.
    @Modifying
    @Query("""
        UPDATE AvailabilitySlot s
        SET s.availableCapacity = s.availableCapacity - :quantity,
            s.status = CASE
                WHEN s.availableCapacity - :quantity = 0
                THEN com.resourcebooking.booking.entity.SlotStatus.FULL
                ELSE com.resourcebooking.booking.entity.SlotStatus.AVAILABLE
            END
        WHERE s.id = :slotId
          AND s.status = com.resourcebooking.booking.entity.SlotStatus.AVAILABLE
          AND s.availableCapacity >= :quantity
        """)
    int reserveCapacity(
            @Param("slotId") Long slotId,
            @Param("quantity") int quantity
    );

    // Restore capacity atomically when a booking is cancelled.
    @Modifying
    @Query(value = """
        UPDATE availability_slots
        SET available_capacity = available_capacity + :quantity,
            status = CASE
                WHEN available_capacity + :quantity = 0
                THEN 'FULL'
                ELSE 'AVAILABLE'
            END,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :slotId
          AND status IN ('AVAILABLE', 'FULL')
          AND available_capacity + :quantity <= capacity
        """, nativeQuery = true)
    int restoreCapacity(
            @Param("slotId") Long slotId,
            @Param("quantity") Integer quantity
    );
}
