#!/usr/bin/env bash
set -euo pipefail

echo "Building Universal APK"
eas build --profile deploy --platform android --non-interactive --no-wait

echo "Building arm64-v8a APK"
eas build --profile deploy-arm64 --platform android --non-interactive --no-wait

echo "Building armeabi-v7a APK"
eas build --profile deploy-armv7 --platform android --non-interactive --no-wait

echo "Building x86 APK"
eas build --profile deploy-x86 --platform android --non-interactive --no-wait

echo "Building x86_64 APK"
eas build --profile deploy-x86_64 --platform android --non-interactive --no-wait

echo "All cloud builds submitted!"
