# postgres

- Repository: https://github.com/postgres/postgres
- Capability: database
- Phase: P1
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-local-foundation**
- Blockers: postgres: local compose profile exists; initialize locally and complete security/license review before use.
- Self-hosting manifest: integrations/compose/docker-compose.yml (profile: core/ledger); initialize and review locally

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/postgres/postgres integrations/upstream/postgres
```

Stable reference guidance: **reviewed immutable HEAD commit 86f7c82cf1023e3599f40f939727791a7090cd44 on upstream branch master (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
