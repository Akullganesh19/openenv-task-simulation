# dolos

- Repository: https://github.com/dolos/dolos
- Capability: compliance
- Phase: P1
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **self-hostable-local-foundation**
- Blockers: dolos: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: dolos — dolos: self-hosting is feasible, but no service image is asserted in this baseline; add a reviewed pinned manifest after runtime validation.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/dolos/dolos integrations/upstream/dolos
```

Stable reference guidance: **unknown/unverified: do not pin until the recorded upstream is confirmed; remote did not return a HEAD reference during 2026-09-09 probe**. The recorded repository URL https://github.com/dolos/dolos could not be resolved to a Git HEAD during the 2026-09-09 network probe (remote did not return a HEAD reference); it may be moved, renamed, unavailable, or require a different canonical owner. Verify the tool-specific upstream before cloning. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
