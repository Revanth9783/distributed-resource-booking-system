# Performance Testing Results

## Test 1: Baseline Read API

**Objective:** Establish baseline performance under light concurrent traffic.

**Environment:**
- Application replicas: 3
- Load balancer: NGINX
- Test tool: Apache JMeter 5.6.3
- Endpoint: `GET /api/slots/resource/1`

**Configuration:**
- Concurrent users: 10
- Ramp-up period: 10 seconds
- Test duration: 60 seconds

**Results:**

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

**Observation:** The read endpoint completed 8,186 requests with no recorded errors during the baseline test. The measured average response time was 67 ms.

**Evidence:** JMeter Summary Report and Aggregate Report screenshots; raw JMeter result file.

**Note:** These results represent read-endpoint performance under the specified test configuration and do not establish maximum system capacity.

## Test 2: 50 Concurrent Users

**Objective:** Evaluate API performance under increased concurrent traffic.

**Test Configuration:**
- Tool: Apache JMeter 5.6.3
- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 50
- Ramp-up period: 25 seconds
- Test duration: 60 seconds

**Results:**

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

**Observation:** The system successfully processed all recorded requests without errors. Response times increased compared with the baseline, while throughput also increased.

**Evidence:** `screenshots/load-test-50-users-summary.png` and `screenshots/load-test-50-users-aggregate.png`

**Raw results:** `jmeter-results/load-test-50-users.jtl`

## Test 3: 500 Concurrent Users

**Objective:** Evaluate the system's response to a high-concurrency burst.

**Configuration:**
- Tool: Apache JMeter 5.6.3
- Endpoint: `GET /api/slots/resource/1`
- Concurrent users: 500
- Ramp-up period: 60 seconds
- Loop count: 1

**Results:**

| Metric | Result |
|---|---:|
| Requests | 500 |
| Average response time | 93 ms |
| Median response time | 45 ms |
| 90th percentile | 217 ms |
| 95th percentile | 343 ms |
| 99th percentile | 704 ms |
| Maximum response time | 928 ms |
| Error rate | 0% |
| Throughput | 8.3 requests/sec |

**Observation:** The system completed all 500 requests without recorded errors during the burst test. This demonstrates successful handling of the tested concurrent request burst.

**Limitation:** This was a one-request-per-user burst, not a sustained load test.

**Evidence:** `load-test-500-users-summary.png` and `load-test-500-users-aggregate.png`

**Raw results:** `jmeter-results/load-test-500-users.jtl`

## Sustained Load Test – 100 Concurrent Users

### Test Configuration
- **Test type:** Sustained load test
- **Concurrent users:** 100
- **Ramp-up period:** 30 seconds
- **Tested endpoint:** GET `/api/slots/resource/1`
- **Tool:** Apache JMeter 5.6.3

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
| Throughput | 206.2 requests/sec |
| Error rate | 0% |

### Observations
- The application handled the sustained load with a reported error rate of 0%.
- The system processed approximately 206 requests per second during this test.
- The average response time was 423 ms.
- The 99th percentile response time was 1,545 ms, indicating that most requests completed within this time.

### Conclusion
The sustained load test demonstrates that the distributed resource booking system successfully handled the tested workload of 100 concurrent users without reported request errors.

### Evidence
- `screenshots/load-test-sustained-100-users-summary.png`
- `screenshots/load-test-sustained-100-users-aggregate.png`

