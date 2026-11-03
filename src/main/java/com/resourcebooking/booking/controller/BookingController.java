
package com.resourcebooking.booking.controller;

import com.resourcebooking.booking.dto.BookingResponse;
import com.resourcebooking.booking.dto.CreateBookingRequest;
import com.resourcebooking.booking.service.BookingService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a booking


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return bookingService.createBooking(request, idempotencyKey);
    }



    // Get booking by ID
    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(
            @PathVariable Long bookingId) {

        return bookingService.getBookingById(bookingId);
    }

    // Get booking history for a user
    @GetMapping("/user/{userId}")
    public List<BookingResponse> getBookingsByUserId(
            @PathVariable Long userId) {

        return bookingService.getBookingsByUserId(userId);
    }

    // Cancel a booking and restore slot capacity
    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable Long bookingId) {

        return bookingService.cancelBooking(bookingId);
    }
}
