## 2026-08-26 — Telemetry and Structured Logging with Structlog
**Blind spot / Bottleneck:** Lack of structured logs and context in `server/app.py` and `core.py`. Important application states (like session handling and WebSocket steps) were outputting plain text or swallowing errors without contextual IDs.
**Instrumentation / Fix:** Integrated `structlog` as the core logger in `server/app.py` and `core.py`. Configured a JSON renderer for production output, and used `.bind(session_id=session_id)` to attach context in WebSocket and REST routes.
**Operational impact:** It's now possible to filter logs by `session_id`, track WebSocket progression, and consume `openenv` output cleanly via Datadog, ELK, or other JSON-based log aggregators.
**Next opportunity:** Expand telemetry tracing to execution times inside `enhanced_grader.py` to identify slowdowns in solution testing.
