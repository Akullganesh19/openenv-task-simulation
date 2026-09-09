# loki

- Repository: https://github.com/loki/loki
- Capability: observability
- Phase: P2
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-with-explicit-runtime-review**
- Blockers: loki: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: loki — loki: requires tool-specific license/provider review and its runtime or external credentials; no production self-hosting is claimed.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/loki/loki integrations/upstream/loki
```

Stable reference guidance: **reviewed immutable HEAD commit 0ef5122b5cd5217277a290d61d6f087b8c8cba9b on upstream branch main (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
