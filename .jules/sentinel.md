## 2024-05-18 — JWT Secret Hardcoding
**Vulnerability class:** Hardcoded Secret
**Entry point:** `server/enhanced_api.py` and `config.py`
**Fix:** Removed default fallback in `config.py` for `SECRET_KEY`, throwing an error if it's missing. Or setting a secure default programmatically.
**Blast radius before fix:** Any instance running OpenEnv in production with default settings would use a known JWT secret, allowing an attacker to forge JWT tokens and impersonate any user or gain admin access.
**Next opportunity:** SQL Injection in `server/enhanced_api.py` via raw queries.
