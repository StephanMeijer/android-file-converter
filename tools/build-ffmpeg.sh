#!/usr/bin/env bash
# build-ffmpeg.sh — Cross-compile FFmpeg for Android arm64-v8a
# Usage: ./tools/build-ffmpeg.sh
# Prereqs: nasm, cmake, pkg-config, autoconf, automake, libtool, Android NDK r28+
set -euo pipefail

# ─── Stage 0: Environment ─────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_DIR="${BUILD_DIR:-/tmp/ffmpeg-android-build}"
OUTPUT_SO="$PROJECT_ROOT/app/src/main/jniLibs/arm64-v8a/libffmpeg.so"

# Auto-detect NDK
NDK_DIR="${ANDROID_NDK_ROOT:-}"
if [ -z "$NDK_DIR" ] && [ -f "$PROJECT_ROOT/local.properties" ]; then
    NDK_DIR=$(grep 'ndk.dir' "$PROJECT_ROOT/local.properties" | cut -d= -f2 | tr -d '[:space:]')
fi
if [ -z "$NDK_DIR" ]; then
    echo "ERROR: Set ANDROID_NDK_ROOT or add ndk.dir to local.properties"
    exit 1
fi
echo "Using NDK: $NDK_DIR"

TOOLCHAIN="$NDK_DIR/toolchains/llvm/prebuilt/linux-x86_64"
CC="$TOOLCHAIN/bin/aarch64-linux-android26-clang"
CXX="$TOOLCHAIN/bin/aarch64-linux-android26-clang++"
AR="$TOOLCHAIN/bin/llvm-ar"
STRIP="$TOOLCHAIN/bin/llvm-strip"
SYSROOT="$TOOLCHAIN/sysroot"
PREFIX="$BUILD_DIR/install"
NJOBS=$(nproc)

mkdir -p "$BUILD_DIR" "$PREFIX/lib" "$PREFIX/include"

# ─── Stage 1: Download sources ────────────────────────────────────────────────
echo "=== Stage 1: Downloading sources ==="

if [ ! -d "$BUILD_DIR/ffmpeg" ]; then
    git clone --depth 1 --branch n7.1 https://git.ffmpeg.org/ffmpeg.git "$BUILD_DIR/ffmpeg"
fi
if [ ! -d "$BUILD_DIR/x264" ]; then
    git clone --depth 1 --branch stable https://code.videolan.org/videolan/x264.git "$BUILD_DIR/x264"
fi
if [ ! -d "$BUILD_DIR/x265" ]; then
    git clone --depth 1 --branch 4.1 https://bitbucket.org/multicoreware/x265_git.git "$BUILD_DIR/x265"
fi
if [ ! -d "$BUILD_DIR/lame" ]; then
    curl -L "https://sourceforge.net/projects/lame/files/lame/3.100/lame-3.100.tar.gz" | tar -xz -C "$BUILD_DIR"
    mv "$BUILD_DIR/lame-3.100" "$BUILD_DIR/lame"
fi
if [ ! -d "$BUILD_DIR/libogg" ]; then
    curl -L "https://downloads.xiph.org/releases/ogg/libogg-1.3.5.tar.gz" | tar -xz -C "$BUILD_DIR"
    mv "$BUILD_DIR/libogg-1.3.5" "$BUILD_DIR/libogg"
fi
if [ ! -d "$BUILD_DIR/libvorbis" ]; then
    curl -L "https://downloads.xiph.org/releases/vorbis/libvorbis-1.3.7.tar.gz" | tar -xz -C "$BUILD_DIR"
    mv "$BUILD_DIR/libvorbis-1.3.7" "$BUILD_DIR/libvorbis"
fi
if [ ! -d "$BUILD_DIR/opus" ]; then
    curl -L "https://downloads.xiph.org/releases/opus/opus-1.5.2.tar.gz" | tar -xz -C "$BUILD_DIR"
    mv "$BUILD_DIR/opus-1.5.2" "$BUILD_DIR/opus"
fi
if [ ! -d "$BUILD_DIR/libvpx" ]; then
    git clone --depth 1 --branch v1.15.0 https://chromium.googlesource.com/webm/libvpx.git "$BUILD_DIR/libvpx"
fi

# ─── Stage 2: Cross-compile codecs ────────────────────────────────────────────
echo "=== Stage 2: Building x264 ==="
cd "$BUILD_DIR/x264"
./configure \
    --host=aarch64-linux-android \
    --cross-prefix="$TOOLCHAIN/bin/llvm-" \
    --sysroot="$SYSROOT" \
    --prefix="$PREFIX" \
    --enable-static --disable-shared --enable-pic \
    --disable-cli --disable-opencl \
    CC="$CC" AR="$AR"
make -j"$NJOBS" && make install

echo "=== Stage 2: Building x265 ==="
mkdir -p "$BUILD_DIR/x265/build/android"
cd "$BUILD_DIR/x265/build/android"
cmake -DCMAKE_TOOLCHAIN_FILE="$NDK_DIR/build/cmake/android.toolchain.cmake" \
    -DANDROID_ABI=arm64-v8a \
    -DANDROID_PLATFORM=android-26 \
    -DCMAKE_INSTALL_PREFIX="$PREFIX" \
    -DENABLE_SHARED=OFF \
    -DENABLE_CLI=OFF \
    -DCMAKE_C_COMPILER="$CC" \
    -DCMAKE_CXX_COMPILER="$CXX" \
    ../../source
make -j"$NJOBS" && make install

echo "=== Stage 2: Building lame ==="
cd "$BUILD_DIR/lame"
./configure \
    --host=aarch64-linux-android \
    --prefix="$PREFIX" \
    --enable-static --disable-shared --enable-nasm \
    --disable-frontend \
    CC="$CC" AR="$AR" STRIP="$STRIP"
make -j"$NJOBS" && make install

echo "=== Stage 2: Building libogg ==="
cd "$BUILD_DIR/libogg"
./configure \
    --host=aarch64-linux-android \
    --prefix="$PREFIX" \
    --enable-static --disable-shared \
    CC="$CC" AR="$AR"
make -j"$NJOBS" && make install

echo "=== Stage 2: Building libvorbis ==="
cd "$BUILD_DIR/libvorbis"
./configure \
    --host=aarch64-linux-android \
    --prefix="$PREFIX" \
    --with-ogg="$PREFIX" \
    --enable-static --disable-shared \
    CC="$CC" AR="$AR"
make -j"$NJOBS" && make install

echo "=== Stage 2: Building opus ==="
cd "$BUILD_DIR/opus"
./configure \
    --host=aarch64-linux-android \
    --prefix="$PREFIX" \
    --enable-static --disable-shared \
    CC="$CC" AR="$AR"
make -j"$NJOBS" && make install

echo "=== Stage 2: Building libvpx ==="
cd "$BUILD_DIR/libvpx"
./configure \
    --target=arm64-android-gcc \
    --prefix="$PREFIX" \
    --enable-static --disable-shared --enable-pic \
    --disable-examples --disable-tools --disable-docs --disable-unit-tests \
    CC="$CC" CXX="$CXX" AR="$AR"
make -j"$NJOBS" && make install

# ─── Stage 3: Patch and build FFmpeg ──────────────────────────────────────────
echo "=== Stage 3: Patching and building FFmpeg ==="
cd "$BUILD_DIR/ffmpeg"

# Patch: rename main() → ffmpeg_execute(), add ffmpeg_cancel()
if grep -q 'int main(' fftools/ffmpeg.c; then
    sed -i 's/int main(int argc, char \*\*argv)/int ffmpeg_execute(int argc, char **argv)/' fftools/ffmpeg.c
    echo '' >> fftools/ffmpeg.c
    echo 'void ffmpeg_cancel(void) { received_sigterm = 1; }' >> fftools/ffmpeg.c
    echo "Patch applied: main() → ffmpeg_execute(), ffmpeg_cancel() added"
else
    echo "Patch already applied, skipping"
fi

PKG_CONFIG_PATH="$PREFIX/lib/pkgconfig" \
./configure \
    --target-os=android \
    --arch=aarch64 \
    --enable-cross-compile \
    --cc="$CC" \
    --cxx="$CXX" \
    --ar="$AR" \
    --strip="$STRIP" \
    --sysroot="$SYSROOT" \
    --prefix="$PREFIX" \
    --enable-gpl \
    --enable-nonfree \
    --enable-libx264 \
    --enable-libx265 \
    --enable-libmp3lame \
    --enable-libvorbis \
    --enable-libopus \
    --enable-libvpx \
    --enable-static \
    --disable-shared \
    --disable-programs \
    --disable-doc \
    --disable-htmlpages \
    --disable-manpages \
    --extra-cflags="-I$PREFIX/include -fPIC -fdata-sections -ffunction-sections" \
    --extra-ldflags="-L$PREFIX/lib -Wl,--gc-sections" \
    --pkg-config-flags="--static"

make -j"$NJOBS"
make install

# ─── Stage 4: Link monolithic .so ─────────────────────────────────────────────
echo "=== Stage 4: Linking monolithic libffmpeg.so ==="

STATIC_LIBS=(
    "$PREFIX/lib/libavformat.a"
    "$PREFIX/lib/libavcodec.a"
    "$PREFIX/lib/libavfilter.a"
    "$PREFIX/lib/libswscale.a"
    "$PREFIX/lib/libswresample.a"
    "$PREFIX/lib/libavutil.a"
)
CODEC_LIBS=(
    "$PREFIX/lib/libx264.a"
    "$PREFIX/lib/libx265.a"
    "$PREFIX/lib/libmp3lame.a"
    "$PREFIX/lib/libvorbis.a"
    "$PREFIX/lib/libvorbisenc.a"
    "$PREFIX/lib/libogg.a"
    "$PREFIX/lib/libopus.a"
    "$PREFIX/lib/libvpx.a"
)

"$CC" -shared -o "$OUTPUT_SO" \
    -Wl,--whole-archive \
    "${STATIC_LIBS[@]}" \
    -Wl,--no-whole-archive \
    "${CODEC_LIBS[@]}" \
    -lz -lm -llog -landroid \
    -Wl,-z,max-page-size=16384 \
    -Wl,--build-id=sha1 \
    -Wl,--strip-all

"$STRIP" "$OUTPUT_SO"

# ─── Stage 5: Copy real headers ───────────────────────────────────────────────
echo "=== Stage 5: Installing real FFmpeg headers ==="
rm -rf "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/libavformat"
rm -rf "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/libavutil"
rm -rf "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/libavcodec"
cp -r "$PREFIX/include/libavformat" "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/"
cp -r "$PREFIX/include/libavutil"   "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/"
cp -r "$PREFIX/include/libavcodec"  "$PROJECT_ROOT/app/src/main/cpp/ffmpeg/"
echo "Headers installed"

# ─── Stage 6: Verify ──────────────────────────────────────────────────────────
echo "=== Stage 6: Verifying output ==="

# Use NDK nm if system nm not available
if command -v nm &>/dev/null; then
    NM="nm"
else
    NM="$TOOLCHAIN/bin/llvm-nm"
fi

REQUIRED_SYMS=(ffmpeg_execute ffmpeg_cancel avformat_open_input)
ALL_OK=1
for SYM in "${REQUIRED_SYMS[@]}"; do
    if "$NM" -D "$OUTPUT_SO" 2>/dev/null | grep -q "$SYM"; then
        echo "  ✓ $SYM"
    else
        echo "  ✗ $SYM MISSING"
        ALL_OK=0
    fi
done

if [ "$ALL_OK" -eq 0 ]; then
    echo "ERROR: Required symbols not found. Check Stage 4 output."
    exit 1
fi

echo "=== Output ==="
ls -lh "$OUTPUT_SO"
echo ""
echo "=== Build complete! ==="
echo "Run: ./gradlew assembleDebug"
