
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.dto.BookingResponse;
import com.resourcebooking.booking.dto.CreateBookingRequest;
import com.resourcebooking.booking.entity.Booking;
import com.resourcebooking.booking.entity.BookingStatus;
import com.resourcebooking.booking.entity.IdempotencyKey;
import com.resourcebooking.booking.entity.IdempotencyStatus;
import com.resourcebooking.booking.entity.OutboxEvent;
import com.resourcebooking.booking.event.SlotCacheInvalidationEvent;
import com.resourcebooking.booking.exception.SlotUnavailableException;
import com.resourcebooking.booking.repository.AvailabilitySlotRepository;
import com.resourcebooking.booking.repository.BookingRepository;
import com.resourcebooking.booking.repository.IdempotencyKeyRepository;
import com.resourcebooking.booking.repository.OutboxEventRepository;
import com.resourcebooking.booking.repository.UserRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BookingService {

    private final AvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    // Custom application metrics
    private final Counter bookingRequests;
    private final Counter successfulBookings;
    private final Counter failedBookings;
    private final Counter idempotentRetries;
    private final Timer bookingProcessingTime;

    public BookingService(
            AvailabilitySlotRepository slotRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            OutboxEventRepository outboxEventRepository,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry) {

        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;

        this.bookingRequests = meterRegistry.counter(
                "booking.requests.total");

        this.successfulBookings = meterRegistry.counter(
                "booking.success.total");

        this.failedBookings = meterRegistry.counter(
                "booking.failure.total");

        this.idempotentRetries = meterRegistry.counter(
                "booking.idempotent.retries.total");

        this.bookingProcessingTime = meterRegistry.timer(
                "booking.processing.time");
    }

    // Create booking with atomic capacity reservation,
    // idempotency and transactional Outbox event.
    @Transactional
    public BookingResponse createBooking(
            CreateBookingRequest request,
            String idempotencyKey) {

        bookingRequests.increment();

        return bookingProcessingTime.record(() -> {

            try {
                BookingResponse response =
                        createBookingInternal(request, idempotencyKey);

                successfulBookings.increment();

                return response;

            } catch (RuntimeException exception) {

                failedBookings.increment();

                throw exception;
            }
        });
    }

    private BookingResponse createBookingInternal(
            CreateBookingRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 200) {

            throw new IllegalArgumentException(
                    "Idempotency-Key must contain 1 to 200 characters");
        }

        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }

        String requestHash = calculateRequestHash(request);

        idempotencyKeyRepository.insertIfAbsent(
                request.getUserId(),
                idempotencyKey,
                requestHash,
                LocalDateTime.now().plusHours(24));

        IdempotencyKey savedKey =
                idempotencyKeyRepository
                        .findByUserIdAndKeyForUpdate(
                                request.getUserId(), idempotencyKey)
                        .orElseThrow(() -> new IllegalStateException(
                                "Could not retrieve idempotency record"));

        if (!savedKey.getRequestHash().equals(requestHash)) {
            throw new IllegalStateException(
                    "Idempotency key was already used with different booking details");
        }

        // Return the original booking for an idempotent retry.
        // Do not create another Outbox event.
        if (savedKey.getStatus() == IdempotencyStatus.COMPLETED) {

            Long originalBookingId = savedKey.getBookingId();

            if (originalBookingId == null) {
                throw new IllegalStateException(
                        "Completed idempotency record has no booking");
            }

            Booking originalBooking = bookingRepository
                    .findById(originalBookingId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Original booking could not be found"));

            idempotentRetries.increment();

            return BookingResponse.from(originalBooking);
        }

        if (savedKey.getStatus() != IdempotencyStatus.PROCESSING) {
            throw new IllegalStateException(
                    "This idempotency key cannot be reused");
        }

        // Reserve capacity atomically.
        int updated = slotRepository.reserveCapacity(
                request.getSlotId(),
                request.getQuantity());

        if (updated == 0) {
            throw new SlotUnavailableException(
                    "Slot is unavailable or has insufficient capacity");
        }

        Booking booking = new Booking();

        booking.setBookingReference(UUID.randomUUID().toString());
        booking.setUserId(request.getUserId());
        booking.setSlotId(request.getSlotId());
        booking.setQuantity(request.getQuantity());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        // Complete idempotency record.
        savedKey.setBookingId(savedBooking.getId());
        savedKey.setStatus(IdempotencyStatus.COMPLETED);

        idempotencyKeyRepository.save(savedKey);

        // Save Outbox event in the SAME database transaction.
        OutboxEvent outboxEvent = new OutboxEvent(
                "BOOKING",
                savedBooking.getId(),
                "BOOKING_CREATED",
                createBookingPayload(savedBooking));

        outboxEventRepository.save(outboxEvent);

        // Invalidate slot cache only after transaction commits.
        eventPublisher.publishEvent(
                new SlotCacheInvalidationEvent(
                        savedBooking.getSlotId() == null
                                ? null
                                : slotRepository.findResourceIdBySlotId(
                                savedBooking.getSlotId())));

        return BookingResponse.from(savedBooking);
    }

    private String createBookingPayload(Booking booking) {

        return String.format(
                "{\"bookingId\":%d,\"bookingReference\":\"%s\","
                        + "\"userId\":%d,\"slotId\":%d,\"quantity\":%d}",
                booking.getId(),
                booking.getBookingReference(),
                booking.getUserId(),
                booking.getSlotId(),
                booking.getQuantity());
    }

    private String calculateRequestHash(CreateBookingRequest request) {

        String canonicalRequest = request.getUserId() + ":"
                + request.getSlotId() + ":"
                + request.getQuantity();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available", exception);
        }
    }

    // Retrieve a booking by ID.
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Booking not found with ID: " + bookingId));

        return BookingResponse.from(booking);
    }

    // Retrieve bookings belonging to a user.
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    // Cancel booking and restore capacity atomically.
    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed bookings can be cancelled");
        }

        int updated = slotRepository.restoreCapacity(
                booking.getSlotId(),
                booking.getQuantity());

        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not restore slot capacity");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);

        // Save cancellation event in the SAME transaction.
        OutboxEvent outboxEvent = new OutboxEvent(
                "BOOKING",
                savedBooking.getId(),
                "BOOKING_CANCELLED",
                createBookingPayload(savedBooking));

        outboxEventRepository.save(outboxEvent);

        // Invalidate slot cache after successful commit.
        Long resourceId = slotRepository.findResourceIdBySlotId(
                savedBooking.getSlotId());

        eventPublisher.publishEvent(
                new SlotCacheInvalidationEvent(resourceId));

        return BookingResponse.from(savedBooking);
    }
}
