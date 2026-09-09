# prometheus

- Repository: https://github.com/prometheus/prometheus
- Capability: observability
- Phase: P2
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-with-explicit-runtime-review**
- Blockers: prometheus: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: prometheus — prometheus: self-hosting is feasible, but no service image is asserted in this baseline; add a reviewed pinned manifest after runtime validation.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/prometheus/prometheus integrations/upstream/prometheus
```

Stable reference guidance: **reviewed immutable HEAD commit 78169fb57d69371b24863b0f26555631a754204c on upstream branch main (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
