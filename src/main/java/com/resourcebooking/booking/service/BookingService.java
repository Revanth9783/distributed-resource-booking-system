
package com.resourcebooking.booking.service;

import com.resourcebooking.booking.dto.BookingResponse;
import com.resourcebooking.booking.dto.CreateBookingRequest;
import com.resourcebooking.booking.entity.Booking;
import com.resourcebooking.booking.entity.BookingStatus;
import com.resourcebooking.booking.exception.SlotUnavailableException;
import com.resourcebooking.booking.repository.AvailabilitySlotRepository;
import com.resourcebooking.booking.repository.BookingRepository;
import com.resourcebooking.booking.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BookingService {

    private final AvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingService(
            AvailabilitySlotRepository slotRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    // Create a booking and reserve capacity atomically.
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("User not found");
        }

        int updated = slotRepository.reserveCapacity(
                request.getSlotId(),
                request.getQuantity()
        );

        if (updated == 0) {
            throw new SlotUnavailableException(
                    "Slot is unavailable or has insufficient capacity"
            );
        }

        Booking booking = new Booking();
        booking.setBookingReference(UUID.randomUUID().toString());
        booking.setUserId(request.getUserId());
        booking.setSlotId(request.getSlotId());
        booking.setQuantity(request.getQuantity());
        booking.setStatus(BookingStatus.CONFIRMED);

        return BookingResponse.from(bookingRepository.save(booking));
    }

    // Retrieve a booking by its ID.
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Booking not found with ID: " + bookingId
                        )
                );

        return BookingResponse.from(booking);
    }

    // Retrieve all bookings belonging to a user.
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Long userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    // Cancel a confirmed booking and restore capacity atomically.
    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Booking not found with ID: " + bookingId
                        )
                );

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Booking is already cancelled"
            );
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Only confirmed bookings can be cancelled"
            );
        }

        int updated = slotRepository.restoreCapacity(
                booking.getSlotId(),
                booking.getQuantity()
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not restore slot capacity"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);

        return BookingResponse.from(bookingRepository.save(booking));
    }
}
