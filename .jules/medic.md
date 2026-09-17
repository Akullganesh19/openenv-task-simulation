## 2024-05-24 — Fix WebSocket session crash on malformed JSON
**Failure mode found:** WebSocket sessions crash entirely when a client sends invalid JSON payload.
**Trigger condition:** Client sending a message that fails `json.loads(data)` on the WS endpoint.
**Fix:** Added a `try...except` block in the WebSocket receive loop catching `json.JSONDecodeError` specifically to send a graceful error message, and a generic `except` block for other processing errors, keeping the connection alive.
**User impact before/after:** Before, the WebSocket connection was terminated abruptly (crash). After, it degraded gracefully by returning an error message allowing the client to continue using the same session.
**Next opportunity:** Need to audit other REST endpoints for missing error handling around JSON payloads and object validation.
