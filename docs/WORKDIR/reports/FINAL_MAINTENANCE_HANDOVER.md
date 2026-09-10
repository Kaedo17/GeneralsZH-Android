# Final maintenance handover

Updated 2026-09-10. Active maintenance is ending at the owner's request.
No ongoing support commitment is implied. Community forks are welcome.
Repository archival is a separate operation.

## Release status

**v0.12**, published 2026-08-16, remains the latest release. The final candidate
is being prepared on `codex/final-maintenance-release`. Do not label it stable
before the checks below have evidence.

Canonical repository: https://github.com/l3ad3r1/GeneralsZH-Android . The nested
local `GeneralsZH-Android-Repo` is an older checkout, not the release source.

## Preserved knowledge

- [Android state](../../STATE.md): dated implementation and device history.
- [Engineering log](../../../android.md): engine discoveries and fixes.
- [Input guide](../../port/ANDROID_INPUT.md): gestures and Android mouse traps.
- [FFmpeg guide](../../port/ANDROID_FFMPEG.md): two silent decoder-stub hazards.
- [DXT3 investigation](../../port/KNOWN_ISSUE_BLACK_MODELS.md): fixed in v0.10;
  retains measurements and rejected explanations.
- [Build procedure](../../../scripts/build/android/README.md): tools, verified
  native-library provenance, alignment and signing.
- [Release checklist](../../port/RELEASE_CHECKLIST.md): required evidence.

Local root `handover.md` and `WORKLOG.md` predate the current release; their crash
hypotheses are not current diagnoses. `PROGRESS.md` concerns separate TCL recovery
work. Preserve these experiments locally without adding them to the release.

## Release invariants

One arm64 APK contains both engines (`libmain.so`, `libmain_generals.so`). Since
v0.11 both tested GPU families use **d3d8to9 + DXVK Native 1.9.2b**. DXVK 2.6 is
retired from shipped Android builds. Default build output and staged libraries
have previously contained 2.6 even when a source change was Java-only.

For launcher-only changes, reuse every native library from published v0.12,
verify its SHA-256 first, and compare every resulting native-library hash.
This preserves the tested runtime; it does not validate new launcher behavior.
Never patch generated dependencies as release source. Preserve the locally
modified DXVK submodule separately unless intentionally reviewed and rebuilt.

Keep the application ID and release signing certificate for in-place upgrades.
Retain the release keystore and credentials privately outside Git. Do not publish
either. Do not uninstall to upgrade: Android deletes app-owned game data, saves,
and replays. A shell backup may omit app-owned files and does not establish that
a reinstall is safe.

## Validation gates

- Build release and run meaningful regression checks for every code fix.
- Verify version, package ID, private GameActivity, native-library provenance,
  diagnostic probes, ZIP alignment and signing certificate.
- Upgrade an existing release in place; check game data and preferences.
- Exercise import completion, cancellation, unreadable input and storage errors.
- Launch Zero Hour, enter skirmish, play, save/load, exit and launch again.
- Exercise suspend/resume, screen lock, audio/video, touch and mouse as available.
- Check Generals using its own base-game profile; shared code does not establish
  its gameplay status.
- Record device/GPU/OS, duration, logs and APK SHA-256. Build success or a menu
  screenshot alone is insufficient evidence of stable gameplay.

At the start of this pass no Android device was connected. The owner plans to
connect one. Candidate runtime testing remains pending until recorded. v0.12's
published evidence covers TCL launch, intro audio/video, repeat launch, file
checks and log export. Its S24 install had no game data and did not validate
Adreno gameplay. v0.11 gameplay results remain historical evidence only.

## Outstanding tracking

GitHub Issues remains the bug tracker. Keep unverified reports open:
[black screen #5](https://github.com/l3ad3r1/GeneralsZH-Android/issues/5),
[lifecycle #2](https://github.com/l3ad3r1/GeneralsZH-Android/issues/2),
[soak/replay #1](https://github.com/l3ad3r1/GeneralsZH-Android/issues/1), and
[renderer packaging #3](https://github.com/l3ad3r1/GeneralsZH-Android/issues/3).
Describe unresolved coverage in the final release without promising future fixes.
