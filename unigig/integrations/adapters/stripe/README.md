# stripe

- Repository: https://github.com/stripe/stripe
- Capability: payments
- Phase: P1
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-local-foundation**
- Blockers: stripe: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: stripe — stripe: requires tool-specific license/provider review and its runtime or external credentials; no production self-hosting is claimed.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/stripe/stripe integrations/upstream/stripe
```

Stable reference guidance: **reviewed immutable HEAD commit 5fb1f79057574a796919ab87bfe8d3c6e5b8f697 on upstream branch master (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
