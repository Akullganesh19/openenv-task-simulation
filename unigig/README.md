<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/de9bcc3e-9e58-46fc-86d8-1de564cd9736

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.

## Integration foundation (iteration 1)

The complete 100-repository integration inventory and phased mapping lives in [`integrations/inventory.csv`](integrations/inventory.csv). Each row has a matching adapter surface under `integrations/adapters/` with clone URL, license/cost verification status, overlap mapping, phase, blockers, and non-secret configuration references. Run `python3 integrations/scripts/validate_inventory.py` to verify the registry.

External services stay behind a backend boundary: Kong routes traffic, Keycloak JWT claims authorize requests, OpenBao resolves `OPENBAO_REF:` values, and Blnk is the only source of truth for money-relevant state. Local foundation services are described in `integrations/compose/docker-compose.yml`; it is intentionally a development manifest and does not claim production support. Clone upstream repositories only after license review and pin a reviewed tag or commit; do not vendor them into the Android module. Never put provider secrets in `.env.example`, source, or the APK.

The Gradle wrapper is included (`./gradlew`). The quality gate is runnable with the wrapper, but requires a locally installed Android SDK (`ANDROID_HOME` or `local.properties`); without that SDK Gradle reports the blocker instead of silently skipping Android validation.
