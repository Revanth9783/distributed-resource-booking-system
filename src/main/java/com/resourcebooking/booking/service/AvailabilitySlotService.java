
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.dto.CreateSlotRequest;
import com.resourcebooking.booking.dto.SlotResponse;
import com.resourcebooking.booking.entity.AvailabilitySlot;
import com.resourcebooking.booking.entity.SlotStatus;
import com.resourcebooking.booking.repository.AvailabilitySlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AvailabilitySlotService {

    private final AvailabilitySlotRepository slotRepository;

    public AvailabilitySlotService(AvailabilitySlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Transactional
    public SlotResponse createSlot(CreateSlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        if (request.getAvailableCapacity() > request.getCapacity()) {
            throw new IllegalArgumentException(
                    "Available capacity cannot exceed capacity"
            );
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setResourceId(request.getResourceId());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity());
        slot.setAvailableCapacity(request.getAvailableCapacity());

        slot.setStatus(
                request.getAvailableCapacity() == 0
                        ? SlotStatus.FULL
                        : SlotStatus.AVAILABLE
        );

        return SlotResponse.from(slotRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsByResource(Long resourceId) {
        return slotRepository.findByResourceId(resourceId)
                .stream()
                .map(SlotResponse::from)
                .toList();
    }
}
