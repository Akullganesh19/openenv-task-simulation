# kong

- Repository: https://github.com/kong/kong
- Capability: gateway
- Phase: P1
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-local-foundation**
- Blockers: kong: local compose profile exists; initialize locally and complete security/license review before use.
- Self-hosting manifest: integrations/compose/docker-compose.yml (profile: core/ledger); initialize and review locally

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/kong/kong integrations/upstream/kong
```

Stable reference guidance: **reviewed immutable HEAD commit 3091de006da341d5f289d446a3d73875a14560e0 on upstream branch master (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
