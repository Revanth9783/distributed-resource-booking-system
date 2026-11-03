package com.resourcebooking.booking.event;

import com.resourcebooking.booking.service.BookingEventProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    private final BookingEventProcessingService processingService;

    public BookingEventConsumer(
            BookingEventProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "booking-events",
            groupId = "booking-event-group"
    )
    public void consumeBookingEvent(String message) {

        processingService.processEvent(message);
    }
}