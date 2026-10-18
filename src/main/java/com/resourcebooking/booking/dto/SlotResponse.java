
package com.resourcebooking.booking.dto;

import com.resourcebooking.booking.entity.AvailabilitySlot;
import com.resourcebooking.booking.entity.SlotStatus;

import java.time.LocalDateTime;

public record SlotResponse(
        Long id,
        Long resourceId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer capacity,
        Integer availableCapacity,
        SlotStatus status
) {
    public static SlotResponse from(AvailabilitySlot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getResourceId(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getCapacity(),
                slot.getAvailableCapacity(),
                slot.getStatus()
        );
    }
}
