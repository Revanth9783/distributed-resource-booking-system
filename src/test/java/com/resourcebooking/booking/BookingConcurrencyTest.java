
package com.resourcebooking.booking;

import com.resourcebooking.booking.dto.CreateBookingRequest;
import com.resourcebooking.booking.entity.AvailabilitySlot;
import com.resourcebooking.booking.entity.Resource;
import com.resourcebooking.booking.entity.ResourceStatus;
import com.resourcebooking.booking.entity.SlotStatus;
import com.resourcebooking.booking.repository.AvailabilitySlotRepository;
import com.resourcebooking.booking.repository.BookingRepository;
import com.resourcebooking.booking.repository.ResourceRepository;
import com.resourcebooking.booking.service.BookingService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BookingConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AvailabilitySlotRepository slotRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void onlyOneBookingSucceedsWhenTwoRequestsCompeteForLastUnit()
            throws Exception {

        Long testResourceId = null;
        Long testSlotId = null;

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startTogether = new CountDownLatch(1);

        try {
            // 1. Create a resource exclusively for this test.
            Resource resource = new Resource();
            resource.setResourceTypeId(1L);
            resource.setName("Concurrency Test " + UUID.randomUUID());
            resource.setDescription("Automated concurrency test");
            resource.setCapacity(1);
            resource.setStatus(ResourceStatus.ACTIVE);
            resource.setLocation("Test");

            Resource savedResource = resourceRepository.saveAndFlush(resource);
            testResourceId = savedResource.getId();

            // 2. Create a slot with exactly one available unit.
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setResourceId(testResourceId);

            LocalDateTime start = LocalDateTime.now()
                    .plusDays(2)
                    .withSecond(0)
                    .withNano(0);

            slot.setStartTime(start);
            slot.setEndTime(start.plusHours(1));
            slot.setCapacity(1);
            slot.setAvailableCapacity(1);
            slot.setStatus(SlotStatus.AVAILABLE);

            AvailabilitySlot savedSlot = slotRepository.saveAndFlush(slot);
            testSlotId = savedSlot.getId();

            // Final variable for use inside lambda expressions.
            final Long slotIdForTest = savedSlot.getId();

            // 3. Use two existing users.
            Long userA = 1L;
            Long userB = 2L;

            Callable<Boolean> requestA = () -> attemptBooking(
                    userA, slotIdForTest, ready, startTogether);

            Callable<Boolean> requestB = () -> attemptBooking(
                    userB, slotIdForTest, ready, startTogether);

            Future<Boolean> resultA = executor.submit(requestA);
            Future<Boolean> resultB = executor.submit(requestB);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should become ready"
            );

            // Release both requests at approximately the same time.
            startTogether.countDown();

            boolean successA = resultA.get(20, TimeUnit.SECONDS);
            boolean successB = resultB.get(20, TimeUnit.SECONDS);

            int successes = (successA ? 1 : 0) + (successB ? 1 : 0);

            assertEquals(
                    1,
                    successes,
                    "Exactly one booking should succeed"
            );

            // 4. Verify the final database state.
            AvailabilitySlot finalSlot = slotRepository
                    .findById(slotIdForTest)
                    .orElseThrow();

            assertEquals(0, finalSlot.getAvailableCapacity());
            assertEquals(SlotStatus.FULL, finalSlot.getStatus());

            assertTrue(
                    finalSlot.getAvailableCapacity() >= 0,
                    "Available capacity must never be negative"
            );

            assertEquals(
                    1,
                    bookingRepository.findBySlotId(slotIdForTest).size(),
                    "Exactly one booking should exist for the slot"
            );

        } finally {
            // Release waiting threads and stop the executor.
            startTogether.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            // 5. Clean up only this test's records, in FK-safe order.
            if (testSlotId != null) {

                // Remove idempotency records associated with this test's bookings.
                jdbcTemplate.update(
                        "DELETE FROM idempotency_keys WHERE booking_id IN " +
                                "(SELECT id FROM bookings WHERE slot_id = ?)",
                        testSlotId
                );

                // Remove bookings before deleting their slot.
                jdbcTemplate.update(
                        "DELETE FROM bookings WHERE slot_id = ?",
                        testSlotId
                );

                slotRepository.deleteById(testSlotId);
            }

            if (testResourceId != null) {
                resourceRepository.deleteById(testResourceId);
            }
        }
    }

    private boolean attemptBooking(
            Long userId,
            Long slotId,
            CountDownLatch ready,
            CountDownLatch startTogether) {

        ready.countDown();

        try {
            if (!startTogether.await(10, TimeUnit.SECONDS)) {
                return false;
            }

            CreateBookingRequest request = new CreateBookingRequest();
            request.setUserId(userId);
            request.setSlotId(slotId);
            request.setQuantity(1);

            bookingService.createBooking(
                    request,
                    "concurrency-" + userId + "-" + UUID.randomUUID()
            );

            return true;

        } catch (Exception exception) {
            // One request is expected to lose the capacity race.
            return false;
        }
    }
}
