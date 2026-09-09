# mattermost

- Repository: https://github.com/mattermost/mattermost
- Capability: collaboration
- Phase: P3
- License/cost: verify upstream license and hosted costs
- Overlap/replacement: evaluate against existing UniGig boundary
- Self-hosting decision: **blocked-unavailable-in-baseline-or-license/provider-review**
- Blockers: mattermost: no reviewed per-tool manifest in this baseline; blocked pending its runtime, license, security, and credential review.
- Self-hosting manifest: blocked record: mattermost — mattermost: requires tool-specific license/provider review and its runtime or external credentials; no production self-hosting is claimed.

## Reproducible acquisition

```sh
git clone --filter=blob:none https://github.com/mattermost/mattermost integrations/upstream/mattermost
```

Stable reference guidance: **reviewed immutable HEAD commit e75e823ff2ad0d373ea3f7ed60c86e45966608a2 on upstream branch master (git ls-remote probe 2026-09-09); pin this SHA before deployment**. The recorded repository responded to git ls-remote on 2026-09-09; this SHA is metadata evidence, not a production compatibility claim. No production support is claimed until the license, runtime, and security review is recorded.

Enable only from the backend: Keycloak claims authorize requests, OpenBao resolves secret references, Kong routes traffic, and Blnk remains the source of truth for money state. Never put provider keys in Android.
