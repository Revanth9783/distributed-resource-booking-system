package com.resourcebooking.booking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcebooking.booking.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingEventProcessingService {

    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    public BookingEventProcessingService(
            ProcessedEventRepository processedEventRepository,
            ObjectMapper objectMapper) {

        this.processedEventRepository = processedEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processEvent(String message) {

        try {
            JsonNode event = objectMapper.readTree(message);

            UUID eventId = UUID.fromString(
                    event.get("eventId").asText());

            String eventType = event.get("eventType").asText();

            int inserted = processedEventRepository.markAsProcessed(
                    eventId,
                    eventType
            );

            if (inserted == 0) {
                System.out.println(
                        "Duplicate event skipped: " + eventId);
                return;
            }

            System.out.println("Processing event: " + eventType);
            System.out.println("Event ID: " + eventId);
            System.out.println("Payload: " + event.get("payload"));

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to process Kafka event", exception);
        }
    }
}