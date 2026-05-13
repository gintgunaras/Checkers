#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build"
SDK="/usr/lib/android-sdk"
PLATFORM="$SDK/platforms/android-23"
BUILD_TOOLS="$SDK/build-tools/debian"
ANDROID_JAR="$PLATFORM/android.jar"
APP_SRC="$PROJECT_DIR/app/src/main"

AAPT="$BUILD_TOOLS/aapt"
DX="$BUILD_TOOLS/dx"
ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"

echo "=== Cleaning build dir ==="
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen"
mkdir -p "$BUILD_DIR/classes"
mkdir -p "$BUILD_DIR/dex"
mkdir -p "$BUILD_DIR/apk"

echo "=== Generating R.java ==="
$AAPT package -f -m \
    -S "$APP_SRC/res" \
    -J "$BUILD_DIR/gen" \
    -M "$APP_SRC/AndroidManifest.xml" \
    -I "$ANDROID_JAR"

echo "=== Compiling Java sources ==="
JAVA_FILES=$(find "$APP_SRC/java" "$BUILD_DIR/gen" -name "*.java" 2>/dev/null | tr '\n' ' ')
javac -source 1.8 -target 1.8 \
    -classpath "$ANDROID_JAR" \
    -d "$BUILD_DIR/classes" \
    $JAVA_FILES

echo "=== Converting to DEX ==="
$DX --dex --output="$BUILD_DIR/dex/classes.dex" "$BUILD_DIR/classes"

echo "=== Packaging APK ==="
UNALIGNED="$BUILD_DIR/apk/checkers-unaligned.apk"
$AAPT package -f \
    -M "$APP_SRC/AndroidManifest.xml" \
    -S "$APP_SRC/res" \
    -I "$ANDROID_JAR" \
    -F "$UNALIGNED"

# Add dex to APK
cd "$BUILD_DIR/dex"
$AAPT add "$UNALIGNED" classes.dex
cd "$PROJECT_DIR"

echo "=== Zipalign ==="
ALIGNED="$BUILD_DIR/apk/checkers-aligned.apk"
$ZIPALIGN -f 4 "$UNALIGNED" "$ALIGNED"

echo "=== Generating debug keystore ==="
KEYSTORE="$BUILD_DIR/debug.keystore"
keytool -genkey -v \
    -keystore "$KEYSTORE" \
    -alias androiddebugkey \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass android \
    -keypass android \
    -dname "CN=Android Debug,O=Android,C=US" \
    2>&1

echo "=== Signing APK with v1+v2 (apksigner) ==="
SIGNED="$PROJECT_DIR/checkers-debug.apk"
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --v1-signing-enabled true \
    --v2-signing-enabled true \
    --out "$SIGNED" \
    "$ALIGNED"

echo "=== Verifying signature ==="
$APKSIGNER verify --verbose "$SIGNED" 2>&1

echo ""
echo "=== BUILD SUCCESSFUL ==="
echo "APK: $SIGNED"
ls -lh "$SIGNED"
