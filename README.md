# REST Service Framework Benchmark

Compare CPU usage, memory usage, cold start, and load capacity across eight REST service frameworks using equivalent endpoints and repeatable workloads.

**Status:** Planning. This repository currently contains this README only. Services, benchmark scripts, and results have not been implemented.

## Frameworks

| Language | Framework | Benchmark configuration to record |
| --- | --- | --- |
| Java | Spring Boot (Spring) | JDK, web stack, server, JVM flags |
| Java | Quarkus | JDK, web stack, JVM flags; native builds measured separately |
| Python | Django | Python version, application server, worker and thread counts |
| Python | Flask | Python version, application server, worker and thread counts |
| Go | Fiber | Go version, build flags, runtime settings |
| Node.js | NestJS | Node.js version, HTTP adapter, process count |
| Node.js | Hono | Node.js version, server adapter, process count |
| Rust | Axum | Rust version, release profile, async runtime settings |

Pin framework, runtime, dependency, and container image versions before collecting results. Treat different adapters, worker counts, and JVM/native modes as separate configurations.

## What to measure

| Metric | Definition | Report |
| --- | --- | --- |
| CPU usage | CPU consumed by the entire service, including every worker | Mean and peak CPU %, CPU seconds per 1,000 successful requests |
| Memory usage | Memory used by the entire service at idle and under load | Idle, mean, and peak MiB; identify measurement source |
| Cold start | Elapsed time from launching a stopped service until its first successful readiness response | Median and p95 milliseconds across fresh launches |
| Load capacity | Highest sustained request rate meeting a declared latency and error target | Successful requests/second, offered rate, p95/p99 latency, error rate |

Report CPU with **100% equal to one fully occupied logical CPU**. A service using two CPUs can reach 200%. Compute CPU efficiency as `CPU seconds / successful requests * 1000`.

For container runs, use the same container-level memory accounting for every framework and record whether filesystem cache is included. Process RSS can be reported separately; summing worker RSS may double-count shared pages. Sample CPU and memory at a fixed interval, initially one second.

Compare resource usage at matching successful throughput as well as at each service's capacity limit. Keep results separate by endpoint and configuration.

## Shared API contract

Every service must implement the same routes and response behavior. These are proposed contracts for the initial suite.

| Method | Route | Workload | Expected behavior |
| --- | --- | --- | --- |
| `GET` | `/health` | Readiness | Return HTTP 200 with `{"status":"ok"}` once ready |
| `GET` | `/json` | Small JSON response | Serialize and return HTTP 200 with `{"message":"Hello, World!"}` |
| `POST` | `/echo` | JSON parsing and serialization | Parse a shared JSON fixture and return the same JSON value with HTTP 200 |
| `GET` | `/items?count=100` | Larger JSON response | Serialize the same deterministic array of 100 items with HTTP 200 |

Use `application/json` and identical data, validation rules, and payload sizes. Define and commit shared fixtures before implementation. JSON routes must exercise serialization on every request; do not substitute pre-encoded responses in individual services.

The initial suite measures HTTP handling, routing, JSON parsing, and serialization. Database, authentication, external API, and disk workloads can be added later as separate scenarios with identical dependencies.

## Fair comparison rules

- Use the same host, operating system, CPU architecture, CPU allocation, and memory limit for every service.
- Run one service at a time. Keep the load generator on a separate machine where possible; otherwise reserve its CPU resources separately.
- Use production servers and release builds. Disable development mode, hot reload, debug features, and request logging consistently.
- Apply identical HTTP version, keep-alive, compression, TLS, request timeout, and payload settings.
- Keep total resource limits fixed when tuning worker or thread counts. Record every tuning change.
- Measure cold starts separately from warmed throughput runs. Give each service the same warm-up procedure and report whether performance stabilized.
- Randomize framework execution order and repeat measurements. Retain raw data and report variation alongside medians.
- Validate response status and body so fast failures or incorrect responses cannot count as throughput.

## Proposed benchmark procedure

Start with these settings and record any changes alongside the results.

### 1. Prepare

1. Implement and validate the shared endpoints for all eight frameworks.
2. Pin dependencies, build production artifacts, and record exact build and launch commands.
3. Give each service 2 logical CPUs and 512 MiB of memory. Record any startup or out-of-memory failures with these limits.
4. Choose a load generator that can schedule request rates, report latency distributions, and export results. Record its version and settings.
5. Record the host hardware, OS, architecture, container runtime, and any VM resource limits.

### 2. Measure cold start

1. Build the service and fetch its image before starting the timer.
2. Start timing immediately before launching a new service container. Stop at the first valid `/health` response.
3. Check readiness every 10 ms. Record that interval and where the probe runs. Allow 60 seconds for startup, and report timeouts separately.
4. Remove the container after each run. Repeat for a total of 30 fresh launches per configuration.
5. Report median and p95 startup times, plus failed launches. Record how long the first `/json` request takes after readiness.

The timer covers container launch through readiness, including container startup overhead. Host filesystem caches stay warm. Image downloads, builds, and machine boot fall outside this measurement. Measure process-only startup or startup with cleared caches in separate experiments.

### 3. Measure idle resources

Once the service is ready, wait 30 seconds for it to settle. Then sample CPU and memory for 60 seconds without benchmark traffic. Use the same monitoring and health probe behavior for every service.

### 4. Measure load capacity

1. Start a fresh service for each endpoint and rate trial. Warm up that endpoint for 30 seconds at 100 requests/second. If this overloads any service, lower the warm-up rate for all services and record the change.
2. Send traffic at a fixed offered rate for 60 seconds. Exclude warm-up from the results. Start at 100 requests/second and double the rate until a run misses a target.
3. Try rates between the last passing rate and the first failing rate. Narrow the gap to within 10% of the last passing rate. If the initial rate fails, halve it until a run passes.
4. Run each candidate rate 5 times. Publish the highest rate that passes all five runs, along with measurements from each run.
5. Record successful throughput, p50/p95/p99 latency, CPU, memory, errors, timeouts, and requests the generator could not send.

For the initial runs, require p95 latency ≤ 100 ms, failed requests < 1%, and no requests dropped by the generator. Count timeouts, connection errors, unexpected status codes, and invalid responses as failures. Set these thresholds before running and use the same ones for every framework.

Keep requests arriving at the scheduled rate even when responses slow down. Measure latency from each request's scheduled arrival, or use a tool that accounts for the delay. Record connection and concurrency limits, and check that the generator has spare capacity. If it saturates, mark service capacity beyond that point as unmeasured.

To compare resource usage, also run all frameworks at the same rates below the lowest measured capacity.

Then run a separate 10-minute sustained-load trial at each configuration's measured capacity. Check for memory growth, throttling, and latency drift. Report whether the service still meets the same targets.

## Results

No measurements published yet. Do not infer a ranking from framework or language choice.

Use one row per framework, configuration, endpoint, and offered rate:

| Framework / configuration | Endpoint | Offered req/s | Successful req/s | p95 / p99 ms | Mean / peak CPU % | Mean / peak MiB | Failed % | Runs passing |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Pending | — | — | — | — | — | — | — | — |

Report startup and idle measurements separately:

| Framework / configuration | Startup median / p95 ms | Failed launches / total | First-request median / p95 ms | Idle mean CPU % | Idle mean MiB |
| --- | --- | --- | --- | --- | --- |
| Pending | — | — | — | — | — |

Each published run should include:

- Commit SHA, date, hardware, OS, architecture, and service resource limits.
- Runtime/framework versions, dependency locks, build flags, server/adapter, workers, and launch commands.
- Load generator version, commands, fixtures, duration, offered rate, concurrency limits, and timeout settings.
- Raw latency and resource samples, failure counts, monitoring definitions, and per-run summaries with median and range across repetitions.
- Startup probe settings, startup failures, and sustained-load observations.

Results describe the tested workload and configuration. Use latency, resource efficiency, startup time, and sustained capacity together when comparing tradeoffs.

## Planned repository layout

```text
services/
  java-spring/
  java-quarkus/
  python-django/
  python-flask/
  go-fiber/
  node-nestjs/
  node-hono/
  rust-axum/
benchmarks/
  fixtures/       # Shared request and response data
  scripts/        # Startup, load, and resource measurement
  config/         # Resource budgets and workload settings
results/
  raw/            # Original outputs and environment metadata
  reports/        # Summary tables and charts
README.md
```

Directories above are planned. Runnable setup and benchmark commands will be added alongside their implementations.

## Implementation checklist

- [ ] Implement equivalent endpoints for all eight frameworks.
- [ ] Add production builds and consistent resource limits.
- [ ] Add shared fixtures and response validation.
- [ ] Automate cold-start, idle, load, and sustained-load measurements.
- [ ] Export raw measurements and generate comparison reports.
- [ ] Publish reproducible commands and initial results.
