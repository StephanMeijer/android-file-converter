# Building FFmpeg for Android

## Prerequisites

- Android NDK r28+ (set `ndk.dir` in `local.properties` or export `ANDROID_NDK_ROOT`)
- Linux x86_64 host (tested on Ubuntu 22.04+ and Arch Linux)
- Build tools: `make`, `cmake`, `nasm`, `pkg-config`, `autoconf`, `automake`, `libtool`
- ~10 GB free disk space for build artifacts
- ~45–60 minutes build time

## Quick Start

### Install build dependencies

**Ubuntu/Debian:**
```bash
sudo apt install build-essential cmake nasm pkg-config autoconf automake libtool
```

**Arch Linux:**
```bash
sudo pacman -S base-devel cmake nasm pkg-config autoconf automake libtool
```

### Build and install

```bash
# Build FFmpeg (downloads sources, cross-compiles, produces libffmpeg.so)
./tools/build-ffmpeg.sh

# Verify required symbols are exported
nm -D app/src/main/jniLibs/arm64-v8a/libffmpeg.so | grep ffmpeg_execute

# Rebuild the Android app
./gradlew assembleDebug
```

## What the Script Does

1. Downloads FFmpeg 7.1 + codec sources (x264, x265, lame, vorbis, ogg, opus, vpx)
2. Cross-compiles each library for Android arm64-v8a using the NDK
3. Patches FFmpeg's `main()` → `ffmpeg_execute()` and adds `ffmpeg_cancel()`
4. Links everything into a single monolithic `libffmpeg.so` (~15–25 MB stripped)
5. Copies real FFmpeg headers to replace build stubs in `app/src/main/cpp/ffmpeg/`

## Build Environment Variables

| Variable | Default | Description |
|---|---|---|
| `ANDROID_NDK_ROOT` | from `local.properties` | Path to Android NDK r28+ |
| `BUILD_DIR` | `/tmp/ffmpeg-android-build` | Working directory for sources and build artifacts |

## Exported Symbols

The `libffmpeg.so` exports these symbols (automatically verified by the build script):

| Symbol | Description |
|---|---|
| `ffmpeg_execute(int, char**)` | CLI entrypoint — replaces `main()` |
| `ffmpeg_cancel(void)` | Abort a running conversion |
| `avformat_open_input(...)` | Used by JNI bridge for duration probing |
| `avformat_find_stream_info(...)` | Stream metadata extraction |
| `avformat_close_input(...)` | Cleanup after probe |

## Codec Versions

| Library | Version | License |
|---|---|---|
| FFmpeg | 7.1 | LGPL 2.1+ / GPL 2+ |
| x264 | stable | GPL 2+ |
| x265 | 4.1 | GPL 2+ |
| LAME (libmp3lame) | 3.100 | LGPL 2+ |
| libogg | 1.3.5 | BSD |
| libvorbis | 1.3.7 | BSD |
| Opus | 1.5.2 | BSD |
| libvpx | 1.15.0 | BSD |

## License

FFmpeg is built with `--enable-gpl`. The resulting binary includes GPL-licensed
codecs (x264, x265). **The entire application must be distributed under GPL v2+.**
See the About screen in the app for full attribution and source links.

## Troubleshooting

**NDK not found:**  
Set `ANDROID_NDK_ROOT` or add `ndk.dir=/path/to/ndk` to `local.properties`.

**Build fails on x265:**  
Ensure `cmake` ≥ 3.13 is installed. x265 uses CMake with the NDK toolchain file.

**Symbol not found after build:**  
Run `nm -D app/src/main/jniLibs/arm64-v8a/libffmpeg.so | grep ffmpeg_execute`.  
If empty, the monolithic link step (Stage 4) failed — check the build log.

**Re-running the script:**  
The script skips downloading source directories that already exist.  
To force a clean rebuild, delete the build directory:
```bash
rm -rf /tmp/ffmpeg-android-build   # or your custom $BUILD_DIR
./tools/build-ffmpeg.sh
```
