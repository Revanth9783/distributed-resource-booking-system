
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.entity.OutboxEvent;
import com.resourcebooking.booking.repository.OutboxEventRepository;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OutboxPublisherService {

    private static final String TOPIC = "booking-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisherService(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEventsAutomatically() {

        List<OutboxEvent> events =
                outboxEventRepository.findPendingEventsForUpdate();

        for (OutboxEvent event : events) {

            try {
                String message = String.format(
                        "{\"eventId\":\"%s\",\"eventType\":\"%s\",\"payload\":%s}",
                        event.getId(),
                        event.getEventType(),
                        event.getPayload()
                );

                kafkaTemplate.send(
                        TOPIC,
                        event.getId().toString(),
                        message
                ).get();

                event.markPublished();

                outboxEventRepository.save(event);

            } catch (Exception exception) {

                throw new IllegalStateException(
                        "Failed to publish Outbox event: "
                                + event.getId(),
                        exception
                );
            }
        }
    }
}
