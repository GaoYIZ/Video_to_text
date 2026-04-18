# Performance Results Template

## Test Environment
- ECS spec:
- OS:
- Java version:
- Python version:
- MySQL version:
- Redis version:
- Whisper model:

## API Admission Benchmark (Job Submit Endpoint)
| concurrency | total requests | success rate | p50 latency | p95 latency | p99 latency |
|---|---:|---:|---:|---:|---:|
| 20 |  |  |  |  |  |
| 50 |  |  |  |  |  |
| 100 |  |  |  |  |  |

## Worker Throughput Benchmark
| worker count | avg queue wait (s) | avg process time (s) | completed jobs | failed jobs |
|---:|---:|---:|---:|---:|
| 1 |  |  |  |  |
| 2 |  |  |  |  |
| 4 |  |  |  |  |

## Recovery Benchmark
- Retry success rate:
- Dead job ratio:
- Mean time to recovery:

## Notes
- Bottleneck observed:
- Most impactful tuning:
- Recommended next step: