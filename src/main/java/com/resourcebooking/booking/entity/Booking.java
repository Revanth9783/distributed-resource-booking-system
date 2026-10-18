
package com.resourcebooking.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", nullable = false, unique = true, length = 50)
    private String bookingReference;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Booking() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public Long getUserId() { return userId; }
    public Long getSlotId() { return slotId; }
    public Integer getQuantity() { return quantity; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
