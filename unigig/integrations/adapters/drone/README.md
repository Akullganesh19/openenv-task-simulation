# drone

- Repository: https://github.com/drone/drone
- Capability: ci
- Phase: P2
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-with-explicit-runtime-review**
- Blockers: drone: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: drone — drone: requires tool-specific license/provider review and its runtime or external credentials; no production self-hosting is claimed.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/drone/drone integrations/upstream/drone
```

Stable reference guidance: **reviewed immutable HEAD commit 3da21b971702d95fcdb6b3e66a036b7f719a0c87 on upstream branch main (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
