# Android build and packaging

Updated 2026-09-10. See the [final handover](../../../docs/WORKDIR/reports/FINAL_MAINTENANCE_HANDOVER.md).
Android runtime is established on specific devices; old scaffolding-only notes
are obsolete. Since v0.11 a single APK uses DXVK Native 1.9.2b on Adreno and Mali.

## Tools

The checked-in app requests Android NDK **28.2.13676358**, compile/target SDK 35,
minimum SDK 24, Java 17 and Gradle 8.7 (Android Gradle Plugin 8.5.2). Use Android
SDK build-tools with `zipalign -P 16` support, such as installed 36.0.0. Native
builds additionally require CMake 3.25+, Ninja, Meson, the source dependencies,
and the hand-built FFmpeg SDK at `ffmpeg-android/`.

Gradle does not configure release signing. Its unsigned output is not a final
installable release artifact. Local wrapper files may exist without being
tracked; a fresh checkout can use installed Gradle 8.7.

## Launcher-only maintenance build

1. Obtain `GeneralsZH-v0.12.apk` from the project's published v0.12 release.
   Verify SHA-256:
   `ce4f7d21bfce0c178da96f0423783eb9bbe0ffc030dc55380b204409ff0f6f21`.
2. Extract its entire `lib/arm64-v8a/` set into
   `android/app/src/main/jniLibs/arm64-v8a/`. Preserve the donor; do not mix in
   unrelated build output. Do not commit staged binaries.
3. From `android/`, run:
   `gradle :app:assembleRelease -PSAGE_SKIP_NATIVE_BUILD=true`.
4. Use the fresh Gradle APK as the base when applying the packaging guard:
   `python scripts/build/android/package-mali-apk.py --base android/app/build/outputs/apk/release/app-release-unsigned.apk --dxvk-from GeneralsZH-v0.12.apk --require-dex-string LauncherActivity --output candidate-unsigned.apk`
5. Compare **every** native-library hash in the candidate against the donor.
   Both engines and all audio/video, C++ and rendering libraries must match.
6. Run `zipalign -f -P 16 4 candidate-unsigned.apk candidate-aligned.apk`, then
   sign to a separate final output with `apksigner sign --ks <private-keystore>`.
   Supply credentials privately. Use the existing release key, not a debug key.
7. Run `zipalign -c -P 16 4 <signed.apk>` and
   `apksigner verify --verbose --print-certs <signed.apk>`. Compare its certificate
   with the v0.12 donor and record SHA-256, package ID and version.
8. Complete the [release checklist](../../../docs/port/RELEASE_CHECKLIST.md).

The packaging script's small-library guard is a heuristic, not proof of the
renderer version. Hash comparison with the verified donor provides that proof
for this launcher-only route. Library size alone is insufficient.

## Native changes

Omit `SAGE_SKIP_NATIVE_BUILD` only when deliberately rebuilding native code.
A native build can stage DXVK 2.6; do not ship it as the unified Android runtime.
The pinned release renderer revisions and patches are listed in the root README.
The repository still lacks a fully automated fresh-source build of every shipped
runtime component; [issue #3](https://github.com/l3ad3r1/GeneralsZH-Android/issues/3)
tracks the packaging work. Preserve provenance instead of claiming otherwise.

FFmpeg is hand-built, despite `RTS_BUILD_OPTION_FFMPEG=OFF` disabling the vcpkg
route. `SAGE_ANDROID_FFMPEG_DIR` selects the real Android decoder sources. Read
[the FFmpeg guide](../../../docs/port/ANDROID_FFMPEG.md) and verify both engines
before shipping a native change; merely including codec libraries does not prove
that either engine was linked against real decoder implementations.

Use the launcher's Storage Access Framework importer for game data. Production
apps do not permit ordinary shell writes to `/data/data/<package>/files`.
Game data and signing material must not be bundled into source or release assets.
