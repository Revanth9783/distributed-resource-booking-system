# Performance and Functional Testing Results

## Project: Distributed Resource Booking System

### Testing Overview

The distributed resource booking system was evaluated using performance, concurrency, and functional correctness tests.

The objectives were to evaluate API response times, throughput, reliability under concurrent workloads, booking capacity enforcement, idempotency, and cancellation behavior.

**Test Environment:**
- Application replicas: 3
- Load balancer: NGINX
- Backend: Spring Boot
- Testing tool: Apache JMeter 5.6.3
- API port: 8082
- Testing interface: JMeter and Windows PowerShell

---

## Test 1: Baseline Read API — 10 Concurrent Users

### Objective

Establish baseline API performance under light concurrent traffic.

### Configuration

- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 10
- Ramp-up period: 10 seconds
- Test duration: 60 seconds

### Results

| Metric | Result |
|---|---:|
| Total requests | 8,186 |
| Average response time | 67 ms |
| Median response time | 63 ms |
| 90th percentile | 96 ms |
| 95th percentile | 111 ms |
| 99th percentile | 170 ms |
| Minimum response time | 12 ms |
| Maximum response time | 674 ms |
| Error percentage | 0% |
| Throughput | 136.3 requests/sec |

### Observations

The application processed 8,186 requests without recorded errors. The average response time was 67 ms, with throughput of 136.3 requests per second.

### Conclusion

The baseline test established reference performance measurements for subsequent load tests.

**Status: PASSED — 0% recorded errors**

### Evidence

- `screenshots/baseline-summary-report.png`
- `screenshots/baseline-aggregate-report.png`
- `jmeter-results/baseline-10-users.jtl`

---

## Test 2: Load Test — 50 Concurrent Users

### Objective

Evaluate API performance under increased concurrent traffic.

### Configuration

- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 50
- Ramp-up period: 25 seconds
- Test duration: 60 seconds

### Results

| Metric | Result |
|---|---:|
| Total requests | 9,717 |
| Average response time | 246 ms |
| Median response time | 178 ms |
| 90th percentile | 537 ms |
| 95th percentile | 618 ms |
| 99th percentile | 850 ms |
| Minimum response time | 8 ms |
| Maximum response time | 1,442 ms |
| Error percentage | 0% |
| Throughput | 161.3 requests/sec |

### Observations

- All 9,717 requests completed without recorded errors.
- Throughput increased compared with the baseline test.
- Average response time increased from 67 ms to 246 ms.
- The 95th percentile response time was 618 ms.

### Conclusion

The system handled the tested workload of 50 concurrent users without recorded request failures, although response latency increased.

**Status: PASSED — 0% recorded errors**

### Evidence

- `screenshots/load-test-50-users-summary.png`
- `screenshots/load-test-50-users-aggregate.png`
- `jmeter-results/load-test-50-users.jtl`

---

## Test 3: High-Concurrency Read API Burst — 500 Users

### Objective

Evaluate the system's response to a high-concurrency request burst.

### Configuration

- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 500
- Ramp-up period: 60 seconds
- Loop count: 1
- Total requests: 500

### Results

| Metric | Result |
|---|---:|
| Total requests | 500 |
| Average response time | 93 ms |
| Median response time | 45 ms |
| 90th percentile | 217 ms |
| 95th percentile | 343 ms |
| 99th percentile | 704 ms |
| Maximum response time | 928 ms |
| Error percentage | 0% |
| Throughput | 8.3 requests/sec |

### Observations

- All 500 requests completed without recorded errors.
- Average response time was 93 ms.
- The test exercised 500 virtual users with one request per user.
- Throughput was 8.3 requests per second over the test execution period.

### Limitation

This was a one-request-per-user burst test rather than a sustained load test. The result does not establish sustained throughput at 500 active users.

### Conclusion

The application successfully handled the tested 500-user burst without recorded request errors.

**Status: PASSED — 0% recorded errors**

### Evidence

- `screenshots/load-test-500-users-summary.png`
- `screenshots/load-test-500-users-aggregate.png`
- `jmeter-results/load-test-500-users.jtl`

---

## Test 4: Sustained Load Test — 100 Concurrent Users

### Objective

Evaluate application performance under sustained concurrent API traffic.

### Configuration

- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 100
- Ramp-up period: 30 seconds
- Test type: Sustained load

### Results

| Metric | Result |
|---|---:|
| Total requests | 24,893 |
| Average response time | 423 ms |
| Median response time | 229 ms |
| 90th percentile | 1,096 ms |
| 95th percentile | 1,208 ms |
| 99th percentile | 1,545 ms |
| Minimum response time | 11 ms |
| Maximum response time | 3,258 ms |
| Error percentage | 0% |
| Throughput | 206.2 requests/sec |

### Observations

- The system processed 24,893 requests without recorded errors.
- Throughput reached approximately 206.2 requests per second.
- Average response time was 423 ms.
- The 99th percentile response time was 1,545 ms.
- Higher throughput was accompanied by increased response latency.

### Conclusion

The system successfully handled the tested sustained workload of 100 concurrent users without recorded errors.

**Status: PASSED — 0% recorded errors**

### Evidence

- `screenshots/load-test-sustained-100-users-summary.png`
- `screenshots/load-test-sustained-100-users-aggregate.png`
- `jmeter-results/load-test-sustained-100-users.jmx`

---

## Test 5: High-Concurrency Booking — 500 Users

### Objective

Evaluate booking behavior under concurrent requests and verify that the system prevents bookings beyond the configured resource capacity.

### Configuration

- Endpoint: `POST /api/bookings`
- Concurrent users: 500
- Total booking requests: 5,000
- Resource ID: 9
- Slot ID: 2021
- Slot capacity: 100
- Initial available capacity: 100
- Booking quantity per request: 1
- Testing tool: Apache JMeter 5.6.3

### JMeter Results

| Metric | Result |
|---|---:|
| Total requests | 5,000 |
| Successful HTTP samples | 100 |
| Failed HTTP samples | 4,900 |
| JMeter error rate | 98.00% |
| Average response time | 79 ms |
| Median response time | 16 ms |
| 90th percentile | 175 ms |
| 95th percentile | 304 ms |
| 99th percentile | 1,318 ms |
| Minimum response time | 11 ms |
| Maximum response time | 3,427 ms |
| Throughput | 166.0 requests/sec |

### Post-Test Capacity Verification

| Verification | Observed Result |
|---|---|
| Initial slot capacity | 100 |
| Confirmed bookings | 100 |
| Total booked quantity | 100 |
| Final available capacity | 0 |
| Final slot status | FULL |
| Additional booking request | HTTP 409 Conflict |
| Overbooking observed | No |

### Observations

1. JMeter executed 5,000 booking requests.
2. Exactly 100 requests were recorded as successful HTTP samples.
3. JMeter classified 4,900 samples as failures.
4. Post-test verification identified 100 confirmed bookings for slot 2021.
5. The total booked quantity was exactly 100.
6. The slot's available capacity reached zero.
7. The slot status changed to FULL.
8. An additional booking attempt after capacity exhaustion returned HTTP 409 Conflict.

### Interpretation of the 98% Error Rate

The 98% JMeter error rate should not automatically be interpreted as a 98% application reliability failure.

The test intentionally submitted 5,000 booking requests against a slot with capacity for only 100 bookings.

The expected maximum number of successful unit bookings was therefore 100.

The remaining 4,900 requests could not all be fulfilled without exceeding capacity.

However, because the individual response codes for all failed samples were not retained, it cannot be confirmed that every failed request was rejected specifically due to insufficient capacity.

### Conclusion

The system maintained the configured capacity limit during the tested high-concurrency workload.

Post-test verification confirmed exactly 100 booked units and zero remaining capacity, with no overbooking observed.

**Status: PASSED — Capacity integrity verified**

### Limitation

The exact HTTP rejection reasons for all 4,900 failed JMeter samples were not independently verified.

### Evidence

- `screenshots/concurrency-500/summary-report.png`
- `screenshots/concurrency-500/aggregate-report.png`
- `screenshots/concurrency-500/slot-capacity-verification.png`
- JMeter test plan: `jmeter-results/Concurrent Booking Request.jmx`

---

## Test 6: Booking Idempotency — PASSED

### Objective

Verify that repeating a booking request with the same idempotency key does not create duplicate bookings or deduct slot capacity more than once.

### Configuration

- Endpoint: `POST /api/bookings`
- Resource ID: 9
- Slot ID: 2022
- Initial available capacity: 10
- Booking quantity: 1
- User ID: 1
- Header: `Idempotency-Key`
- Both requests used the same generated UUID as the idempotency key.

### Test Procedure

1. Generated a unique idempotency key.
2. Submitted the first booking request.
3. Recorded the returned booking ID and booking reference.
4. Repeated the same request using the same idempotency key.
5. Compared the two booking responses.
6. Retrieved the slot's remaining capacity.

### Observed Results

| Verification | Result |
|---|---|
| First booking ID | 1396 |
| Second booking ID | 1396 |
| Same booking ID | True |
| Same booking reference | True |
| First booking status | CONFIRMED |
| Second booking status | CONFIRMED |
| Initial available capacity | 10 |
| Final available capacity | 9 |
| Capacity deducted | 1 |

### Observations

- Both requests returned booking ID 1396.
- Both requests returned the same booking reference.
- The booking remained CONFIRMED.
- Available capacity decreased from 10 to 9.
- Repeating the request did not deduct capacity a second time.

### Conclusion

The application successfully handled the repeated booking request using the same idempotency key.

The test demonstrated duplicate booking prevention and single capacity deduction for the tested retry scenario.

**Status: PASSED**

### Evidence

- `screenshots/idempotency-test-complete.png`

---

## Test 7: Booking Cancellation and Capacity Restoration — PASSED

### Objective

Verify that cancelling a confirmed booking updates the booking status and restores the previously reserved slot capacity.

### Configuration

- Cancellation endpoint: `POST /api/bookings/1396/cancel`
- Verification endpoint: `GET /api/bookings/1396`
- Resource ID: 9
- Slot ID: 2022
- Booking ID: 1396
- Total slot capacity: 10
- Booking quantity: 1

### Test Procedure

1. Used the confirmed booking created during the idempotency test.
2. Verified that the slot had 9 available units.
3. Submitted the booking cancellation request.
4. Retrieved the updated slot capacity.
5. Retrieved the booking by its ID to verify its final status.

### Observed Results

| Verification | Before | After |
|---|---|---|
| Booking status | CONFIRMED | CANCELLED |
| Available capacity | 9 | 10 |
| Total capacity | 10 | 10 |
| Slot status | AVAILABLE | AVAILABLE |

### Observations

- Booking 1396 was successfully cancelled.
- The booking status changed to CANCELLED.
- Available capacity increased from 9 to 10.
- The restored capacity did not exceed the configured maximum.
- A separate GET request confirmed the booking's CANCELLED status.

### Conclusion

The application successfully cancelled the confirmed booking and restored the reserved slot capacity.

The test demonstrated correct capacity restoration and booking state transition for the tested cancellation.

**Status: PASSED**

### Evidence

- `screenshots/cancellation-capacity-restoration.png`

---

## Overall Testing Summary

| Test | Workload | Result |
|---|---|---|
| Baseline read API | 10 users | PASSED |
| Read API load test | 50 users | PASSED |
| Read API burst test | 500 users | PASSED |
| Sustained read API test | 100 users | PASSED |
| High-concurrency booking | 5,000 requests | PASSED — capacity integrity |
| Booking idempotency | 2 requests, same key | PASSED |
| Cancellation and capacity restoration | 1 booking | PASSED |

## Key Performance Findings

- Highest observed read API throughput: **206.2 requests/sec** during the 100-user sustained load test.
- Largest recorded read workload: **24,893 requests**, with 0% reported errors.
- Largest tested booking workload: **5,000 requests** from 500 virtual users.
- Confirmed bookings under capacity contention: **100 out of 100 available units**.
- Observed overbooking: **None**.
- Repeated requests with the same idempotency key returned the same booking.
- Cancellation successfully restored reserved capacity.

## Testing Limitations

1. Performance results represent the specific local test environment and configurations.
2. The 500-user read burst test was not a sustained load test.
3. The HTTP rejection reasons for all 4,900 failed booking samples were not independently verified.
4. The tests do not establish the system's absolute maximum throughput or concurrency limit.
5. The idempotency and cancellation tests verify the specific scenarios executed rather than every possible failure or retry scenario.

## Overall Conclusion

The distributed resource booking system successfully completed the documented performance and functional tests.

The read APIs handled the tested concurrent workloads without recorded request failures. The booking concurrency test demonstrated capacity integrity, with exactly 100 units booked against a capacity of 100 and no observed overbooking.

Functional verification also demonstrated that repeated requests using the same idempotency key returned the existing booking and that cancellation restored reserved capacity correctly.

These results provide supporting evidence for the system's tested performance, concurrency control, idempotency, and booking lifecycle behavior.

---

## Pending Testing

- Duplicate cancellation protection
- Additional failure-handling or resilience tests, if required

## Evidence Directory

Test plans, available raw results, and screenshots are stored under:

`docs/results/`

All results should be retained for the final project report, poster, and GitHub README.

## Test 7: NGINX Load Balancing Verification — PASSED

### Objective
Verify that NGINX distributes incoming HTTP requests across three Spring Boot application replicas.

### Test Configuration
- Reverse proxy: NGINX
- Application replicas: 3
- Endpoint: `GET /api/slots/resource/9`
- Total requests: 100
- Entry point: `http://localhost:8082`

### Results

| Metric | Result |
|---|---|
| Total requests | 100 |
| Successful requests | 100 |
| Failed requests | 0 |
| Success rate | 100% |
| Backend replicas observed | 3 |
| HTTP status | 200 |

### Verified Backend Distribution

| Replica | Docker IP |
|---|---|
| booking-app-1 | 172.18.0.5 |
| booking-app-2 | 172.18.0.2 |
| booking-app-3 | 172.18.0.9 |

NGINX access logs confirmed that requests were forwarded to all three backend IP addresses.

The NGINX upstream configuration included all three application replicas and used the default weighted round-robin load-balancing method.

### Conclusion
The test verified successful HTTP request handling through NGINX and demonstrated that traffic reached all three application replicas. All 100 requests completed successfully, with no recorded failures.

The test confirms request distribution across three replicas but does not measure exact per-replica traffic percentages.

**Status: PASSED**

### Evidence
- `docs/results/screenshots/nginx-load-balancing-verification.png`
- `docs/results/screenshots/nginx-replica-ip-mapping.png`


## Test 8: Kafka Event Publication and Transactional Outbox Verification — PASSED

**Objective:** Verify that booking events are published to Kafka and that the transactional outbox records are processed.

### Kafka Topic Verification

The following Kafka topics were confirmed:

- `booking-events`
- `booking-events.DLT`
- `__consumer_offsets`

### Kafka Event Verification

The Kafka console consumer successfully retrieved 119 messages from the `booking-events` topic.

Observed events included `BOOKING_CREATED` messages containing event IDs, booking IDs, slot IDs, user IDs, quantities, and booking references.

Events associated with slot `2021`, used during concurrent booking testing, were observed.

### Transactional Outbox Verification

Application logs confirmed repeated polling of the `outbox_events` table using `FOR UPDATE SKIP LOCKED`.

PostgreSQL verification returned:

| Status | Count |
|---|---:|
| PUBLISHED | 112 |
| PENDING | 0 |

All 112 outbox records were marked `PUBLISHED` at the time of verification.

### Conclusion

**PASSED:** Kafka event publication and retrieval were verified, and the database showed no pending outbox records.

Application consumer processing, delivery guarantees under failure, and crash recovery were not independently verified.

### Evidence

- `docs/results/screenshots/kafka-event-delivery.png`
- `docs/results/screenshots/kafka-outbox-published.png`