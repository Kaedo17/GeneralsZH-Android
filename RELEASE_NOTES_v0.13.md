# GeneralsZH Android v0.13

This is the final maintenance release for the Android port.

## Fixed

- Interrupted or short game-file imports no longer replace a usable archive.
- Verified mod downloads are promoted transactionally; cancelled or failed
  downloads leave the previous file intact.
- Launcher background work is safe when Android recreates the activity, and
  conflicting controls are disabled while an operation runs.
- Archive extension matching is stable under Turkish and other locale rules.
- Profile and mod names `.` and `..` are rejected.
- Equal-size files are copied again when explicitly imported, allowing a damaged
  same-size archive to be repaired.

## Runtime and upgrade

The APK is arm64-v8a, signed with the same certificate as v0.12, and keeps the
tested d3d8to9 + DXVK Native 1.9.2b runtime. Install it over v0.12 with `adb
install -r` or the Android package installer. **Do not uninstall the app or clear
its data:** Android removes GameData, saves, replays, profiles and preferences.

On the TCL NXTPAPER test device, the in-place upgrade preserved 487 recorded
GameData/Mods/Profile files and launcher settings. The launcher file check passed
42 archives. The game entered GameActivity and remained alive through a 12-second
launch smoke test.

Full skirmish, save/load, suspend/resume and extended soak coverage were not run
for this candidate. Huawei MatePad issue #5 remains open after a Vulkan
`VK_ERROR_DEVICE_LOST` during map loading; this release does not claim to fix it.

Candidate SHA-256:

`88f753eb24d235811c796b88f236dd3805952dfd211a4cf69589a0185a8c2dd3`
