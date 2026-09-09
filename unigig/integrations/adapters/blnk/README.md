# blnk

- Repository: https://github.com/blnk/blnk
- Capability: ledger
- Phase: P1
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-local-foundation**
- Blockers: blnk: local compose profile exists; initialize locally and complete security/license review before use.
- Self-hosting manifest: integrations/compose/docker-compose.yml (profile: core/ledger); initialize and review locally

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/blnk/blnk integrations/upstream/blnk
```

Stable reference guidance: **unknown/unverified: do not pin until the recorded upstream is confirmed; remote did not return a HEAD reference during 2026-09-09 probe**. The recorded repository URL https://github.com/blnk/blnk could not be resolved to a Git HEAD during the 2026-09-09 network probe (remote did not return a HEAD reference); it may be moved, renamed, unavailable, or require a different canonical owner. Verify the tool-specific upstream before cloning. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
