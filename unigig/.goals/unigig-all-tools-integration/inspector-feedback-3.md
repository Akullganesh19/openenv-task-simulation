# Inspector verdict — iteration 3

Builder commit inspected: `688e910c00228fd89546ebe2ac23bfe3f2191065` (`feat(integrations): [B] enforce runtime policy and honest tool status`).

## Validation evidence

- `python3 integrations/scripts/validate_inventory.py` — **PASS**: 100 rows, 100 unique IDs, 100 adapter surfaces, required fields, and no committed secrets.
- Independent adapter audit — **PASS**: every inventory row has a matching `README.md` and `config.env.example`; repository URL and exact clone command match each adapter README.
- Inventory metadata audit — **PASS for coverage, FAIL for stable-reference quality**: all 100 rows have unique, ID-interpolated rationale/guidance and tool-specific self-hosting records. However every `stable_ref_status` remains `unknown/unverified`, and the rationale is the same generic offline template with only the tool ID substituted; no reviewed tag, branch, or immutable commit is recorded.
- `python3 -m unittest discover -s integrations/policy -p 'test_*.py' -v` — **PASS**: 5 runtime policy tests.
- `python3 integrations/policy/validate_contract.py` — **PASS**: contract markers plus middleware tests.
- `python3 -m compileall -q integrations/scripts integrations/policy` — **PASS**.
- Runnable boundary probe — **PASS for normal requests/fail-closed authorization**: `python3 -m integrations.policy.server` returned 200/`allowed: true` for valid claims and 403/`allowed: false` for a wrong issuer. Missing `KEYCLOAK_ISSUER`/`ALLOWED_ROLES` is rejected. A malformed top-level JSON array causes a handled-by-server connection close rather than authorization, but does not grant access.
- `git diff --check 688e910^ 688e910` — **PASS**.
- Tracked credential-pattern scan — **PASS**: no private-key/provider-token patterns; no `api.example.invalid` Android fallback remains.
- `./gradlew --version` — **PASS** (Gradle 9.3.1 wrapper runs).
- `./gradlew testDebugUnitTest lintDebug assembleDebug` — **BLOCKED/FAILS CLOSED** without `UNIGIG_BACKEND_URL`; with an explicit URL it reaches the expected missing Android SDK blocker (`ANDROID_HOME`/`local.properties` unavailable). No backend URL literal fallback is present.
- Docker Compose validation — **BLOCKED**: Docker executable is unavailable.
- OPA validation — **BLOCKED**: `opa` executable is unavailable; the Python middleware is the runnable enforcement path.

## Acceptance criteria

1. **PASS** — complete 100-row inventory and required fields.
2. **PASS** — isolated adapter/configuration surfaces exist for all 100 tools without client-side secrets.
3. **PASS (with scope note)** — executable Python boundary enforces issuer, identity, role, expiry, OpenBao reference scope, and Blnk-only money operations. Kong remains a documented prerequisite for JWT signature verification; no independent OPA runtime was available.
4. **PASS** — five foundation services have a Compose manifest; the other 95 entries carry explicit tool-specific blocked records rather than fabricated manifests.
5. **PASS** — Android backend URL is an explicitly required build input, with no hardcoded backend fallback or provider credentials.
6. **FAIL** — stable-reference guidance is not sufficient: all 100 entries are `unknown/unverified` and use a generic rationale template with only the ID substituted. No exact reviewed stable branch/tag/commit is supplied where discoverable.
7. **PASS with blockers** — wrapper, Python, inventory, policy, diff, and secret gates ran; Android SDK, Docker, and OPA are unavailable and documented above.

## Verdict

FAIL — iteration 3 resolves runtime enforcement, explicit Android configuration, and per-tool self-hosting records, but does not satisfy the required non-generic stable-reference rationale/review for all 100 tools.
