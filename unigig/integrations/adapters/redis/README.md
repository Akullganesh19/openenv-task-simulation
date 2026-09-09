# redis

- Repository: https://github.com/redis/redis
- Capability: cache
- Phase: P2
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-with-explicit-runtime-review**
- Blockers: redis: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: redis — redis: self-hosting is feasible, but no service image is asserted in this baseline; add a reviewed pinned manifest after runtime validation.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/redis/redis integrations/upstream/redis
```

Stable reference guidance: **reviewed immutable HEAD commit 48cc562064dbb52ef3f9cc1a5aa32db6811f5871 on upstream branch unstable (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
