# Monitoring Snapshot: bookmark-manager

> Generated: 2026-07-04
> Source: interim proxy — Newman API-test run from the pre-push `api-tester` gate (round 2).
> There is no live deployment yet, so these are pre-deploy CI-run numbers, not production metrics.

## Data source

Structured logs are emitted per `implementation.md`'s logging spec:
- `component=http` — `method`, `path`, `status`, `latency_ms` (one line per request)
- `component=store` — `op`, `url`, `result` (ok | duplicate_url | not_found | validation | io_error)
- `component=cli` — `subcommand`, `result`, `exit_code`

Until a live deployment exists, the Newman run against the local `ServerKt` server is the
available signal for error count and latency.

## Snapshot (Newman round-2 run)

| Metric | Value |
|---|---|
| Requests executed | 10 |
| Failed requests | 0 |
| Assertions | 15 passed / 0 failed |
| Error rate | 0% |
| Avg response time | ~10 ms |
| Min / Max response time | 3 ms / 35 ms |
| Total run duration | 239 ms |

All negative-path cases (409 duplicate, 404 not-found, 400 bad-request) returned their
expected status codes — i.e. these are correct responses, not errors.

## Alert thresholds

Derived from the NFR "handles up to a few hundred bookmarks without noticeable latency"
(no hard latency bound is stated in requirements.md, so the latency threshold is a soft
operational default, not a contractual SLO):

- **Error rate > 5%** over a rolling window → alert. (`component=http status>=500` or
  `component=store result=io_error`; 4xx client errors are expected and excluded.)
- **Max request latency > 500 ms** → alert (soft default for "no noticeable latency").
- **Any `result=io_error`** → alert immediately (indicates a corrupt/unreadable store file).

## Follow-ups (require a live deployment)

- Real infrastructure metrics — CPU, memory, container restarts, cost — require the deploy
  target's own monitoring (e.g. Render dashboard). Not available until the gated deploy step
  runs against a real environment; do not fabricate these numbers.
- Once deployed, replace this interim Newman-based snapshot with parsed production logs and
  wire the thresholds above into the platform's alerting.
