# Final Android candidate validation

Status: in progress, 2026-09-11. This is a record of observed results, not a
declaration that every release gate has passed.

## Baseline and preservation

- Source baseline: `2b3b25ad5`, branch `codex/final-maintenance-release`.
- Published donor: `GeneralsZH-v0.12.apk`, SHA-256
  `ce4f7d21bfce0c178da96f0423783eb9bbe0ffc030dc55380b204409ff0f6f21`.
- Installed TCL v0.12 APK was pulled before testing. Its release certificate
  matches the published donor: SHA-256
  `b82277491a6e25094ab2521b32e7728f4e9eb165cb950f544badfcf4564a5374`.
- Device: TCL NXTPAPER 9469X / Bellona, Android 15, Mali-G57 (historical GPU
  identification). Existing package `me.generalsx.zh`, versionCode 12.
- Before upgrade, 487 file paths/sizes recorded under GameData, Mods and Profiles.
  Local evidence: `logs/final-maintenance/data-inventory-before.txt`.
- Launcher baseline: Zero Hour, GameData (31 archives), Shockwave mod; skip intro
  on, animated menu enabled, windowed off, shadow volumes disabled, extra arguments
  empty. UI evidence saved under `logs/final-maintenance/`.
- Existing engine log and installed APK preserved locally. These are diagnostic
  backups, not a complete backup of private preferences or game saves.
- No uninstall, app-data clearing, or device bootloader work is permitted.
- Baseline launcher file check passed: 42 archives (31 GameData + 11 Shockwave),
  zero reported header/size problems. This does not validate archive contents.
- All 16 staged native libraries match the published v0.12 APK byte for byte.
- A local Git bundle preserves the complete pre-final source history:
  `logs/final-maintenance/pre-final-source.bundle` (61,888,938 bytes), verified
  with `git bundle verify`. Existing uncommitted DXVK changes are separately
  preserved at `logs/final-maintenance/pre-existing-dxvk.patch` with SHA-256
  `5306e538f3697fccca2d750d66fb39af425e750e2c228eed402caddfe604e064`.
- Candidate `0.13-android` installed in place over v0.12 on TCL NXTPAPER
  (`987800005DB3824`) successfully. `firstInstallTime` remained
  `2026-07-30 02:17:28`; package reported versionCode 13.
- Post-upgrade launcher retained the same profile, mod and startup toggles.
  It started GameActivity in the same task/process; the native process remained
  alive for the 12-second smoke window. This is not skirmish/soak evidence.
- Candidate APK SHA-256: `88f753eb24d235811c796b88f236dd3805952dfd211a4cf69589a0185a8c2dd3`.
- APK verification passed with v2/v3 signatures and the v0.12 certificate.

## Unresolved external report

[Issue #5](https://github.com/l3ad3r1/GeneralsZH-Android/issues/5) includes a
Huawei MatePad log ending in
`DxvkSubmissionQueue: Command submission failed: VK_ERROR_DEVICE_LOST` after map
loading. This confirms a device-loss failure, not its root cause. The launcher
file-copy fixes do not establish a fix for that report. Keep it open.

## Candidate evidence

Build, regression tests, signed artifact identity, in-place upgrade and gameplay
results will be recorded here as they complete.
