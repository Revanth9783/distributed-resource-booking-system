
package com.resourcebooking.booking.controller;

import com.resourcebooking.booking.dto.CreateSlotRequest;
import com.resourcebooking.booking.dto.SlotResponse;
import com.resourcebooking.booking.service.AvailabilitySlotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    public AvailabilitySlotController(AvailabilitySlotService slotService) {
        this.slotService = slotService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse createSlot(
            @Valid @RequestBody CreateSlotRequest request) {
        return slotService.createSlot(request);
    }

    @GetMapping("/resource/{resourceId}")
    public List<SlotResponse> getSlotsByResource(
            @PathVariable Long resourceId) {
        return slotService.getSlotsByResource(resourceId);
    }
}
