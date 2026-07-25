#!/usr/bin/env bash
set -euo pipefail

build() {
    local abi="$1"

    if [[ -z "$abi" ]]; then
        echo "Building Universal APK"
        eas build --profile deploy --platform android --non-interactive --no-wait
    else
        echo "Building $abi APK"
        ANDROID_ABIS="$abi" \
            eas build --profile deploy --platform android --non-interactive --no-wait
    fi
}

build ""
build "arm64-v8a"
build "armeabi-v7a"
build "x86"
build "x86_64"

echo "✅ All cloud builds submitted!"
