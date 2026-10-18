
package com.resourcebooking.booking.dto;

import com.resourcebooking.booking.entity.Booking;
import com.resourcebooking.booking.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookingReference,
        Long userId,
        Long slotId,
        Integer quantity,
        BookingStatus status,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getUserId(),
                booking.getSlotId(),
                booking.getQuantity(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
