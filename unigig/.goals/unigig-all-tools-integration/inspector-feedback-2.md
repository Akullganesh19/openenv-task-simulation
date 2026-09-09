# Inspector verdict — iteration 2

Builder commit inspected: `ae8204cceb24ade03b31c35350adcf4ba495dc5d` (`feat(integrations): [B] enforce tool acquisition and backend policies`).

## Validation evidence

- `python3 integrations/scripts/validate_inventory.py` — **PASS** (`100 rows, 100 adapter surfaces, no secrets`).
- Independent adapter audit — **PASS**: 100 unique inventory IDs, 100 matching adapter directories, and every adapter README contains the exact inventory URL, clone command, stable-reference text, and self-hosting decision.
- `python3 integrations/policy/validate_contract.py` — **PASS**, but this is a marker/static check only.
- `python3 -m compileall -q integrations/scripts integrations/policy` — **PASS**.
- `git diff --check` — **PASS** for the inspected project changes.
- Credential-pattern scan — **PASS**: no private keys, provider-key formats, or obvious token literals found.
- `./gradlew --version` — **PASS**: restored wrapper runs Gradle 9.3.1.
- `./gradlew testDebugUnitTest lintDebug assembleDebug` — **BLOCKED**: Android SDK location is unavailable (`ANDROID_HOME`/`local.properties` missing).
- Docker/Compose gate — **BLOCKED**: Docker is not installed; `docker compose config` could not run.
- OPA gate — **BLOCKED**: `opa` is not installed, and no application/backend wiring executes `integrations/policy/unigig.rego`.
- Android URL audit — **FAIL finding**: `app/build.gradle.kts:25` still contains the literal fallback `https://api.example.invalid/`; the production URL is configurable through `UNIGIG_BACKEND_URL`, but the client still hardcodes a backend URL fallback.

## Acceptance criteria

1. **PASS** — `integrations/inventory.csv` has 100 unique repositories and all required inventory fields.
2. **PASS (surface level)** — each row has an isolated adapter README and environment example, with no committed secrets. These remain configuration/acquisition surfaces, not upstream integrations.
3. **FAIL** — policy is documented and represented by OpenAPI/Rego markers, but is not actually enforceable in a running boundary. Kong verifies expiry and uses `iss` as a JWT key claim but does not enforce the required Keycloak issuer, `preferred_username`, or `realm_access.roles`; the OpenAPI `x-required-keycloak-claims` extension is non-enforcing; `validate_contract.py` only searches strings; and no runtime loads the Rego policy. OpenBao reference validation and Blnk-only money authorization likewise have schema/prose checks but no backend enforcement.
4. **FAIL** — the inventory now has a per-tool self-hosting decision, but 53 tools are classified as self-hostable with runtime review while their manifest is only `blocked/pending: review required`, `none yet`, or unavailable infrastructure. The Compose file covers only the small core foundation (Postgres, Keycloak, OpenBao, Kong, Blnk), so feasible self-hostable tools do not each have a safe manifest or a tool-specific blocked rationale.
5. **FAIL** — Android delegates calls through `BuildConfig.UNIGIG_BACKEND_URL` and contains no provider credentials, but the fallback backend URL remains hardcoded in the Android build configuration.
6. **FAIL** — every tool has an exact clone command, but all 100 entries use the same generic guidance: “Discover upstream releases/tags at clone time.” No reviewed stable branch/tag/commit is supplied where discoverable, so the required exact stable-reference guidance is absent.
7. **PASS with blocker** — the requested Gradle gate was attempted using the restored wrapper and is blocked only by the missing Android SDK; static Python, inventory, policy-marker, diff, and secret checks pass. Docker validation is also blocked by the missing Docker executable.

## Required follow-up

Make the authorization/secret/ledger policy executable at the backend or gateway (including issuer and claim checks and restricted OpenBao-reference validation), remove the Android URL literal entirely in favor of an explicit required build/local configuration, provide reviewed tag/commit guidance per tool, and add manifests or explicit tool-specific blocked status for every self-hosting decision that is not actually available.

FAIL
