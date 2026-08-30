## 2024-05-24 — Prevent Swallowed HTTP Exceptions in API

**Failure mode found:** Meaningful HTTP 400 and 404 errors in API endpoints are currently swallowed by broad `except Exception as e:` blocks, resulting in generic 500 Internal Server Errors.
**Trigger condition:** Providing a duplicate username during registration, or providing an invalid session ID during task submission.
**Fix:** Explicitly catch and re-raise `HTTPException` before the broad `Exception` block in `register_user` and `submit_solution`.
**User impact before/after:** Users previously received confusing 500 Internal Server Errors (crash). Now they receive degraded-but-usable 400 and 404 error codes with precise messages.
**Next opportunity:** Reviewing other API endpoints for similar swallowed exception logic.
