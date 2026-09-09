## 2026-09-09 — Insecure Password Storage
**Vulnerability class:** Authentication/Crypto (Insecure Hashing Algorithm)
**Entry point:** `server/enhanced_api.py` (`pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")`)
**Fix:** Change bcrypt to Argon2
**Blast radius before fix:** Passwords hashed with bcrypt can be cracked quickly if the DB is compromised. Argon2 is the industry standard.
**Next opportunity:** Need to examine JWT token secret usage.
