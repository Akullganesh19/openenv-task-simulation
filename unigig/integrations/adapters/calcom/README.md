# calcom

- Repository: https://github.com/calcom/cal.com
- Capability: scheduling
- Phase: P3
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **blocked-unavailable-in-baseline-or-license/provider-review**
- Blockers: calcom: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: calcom — calcom: requires tool-specific license/provider review and its runtime or external credentials; no production self-hosting is claimed.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/calcom/cal.com integrations/upstream/calcom
```

Stable reference guidance: **reviewed immutable HEAD commit 1251ba5be567d26a7f922452fe7797642376476e on upstream branch main (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
