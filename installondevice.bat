@REM WARNING: Script is Deprecated!! From 2025-07-29T12:45:00Z.
@REM Checkout: https://github.com/PasscodesApp/Passcodes/blob/main/docs/building.md.

@REM Build & install on Android phone

echo "WARNING: Script is Deprecated!! From 2025-07-29T12:45:00Z. More Info:- https://github.com/PasscodesApp/Passcodes/blob/main/docs/building.md."

pause

@echo off

@REM Force continuation after Gradle command by capturing the output
if "%~1"=="prod" (
    echo building a production build
    call gradlew clean assembleRelease
    if ERRORLEVEL 1 (
        echo Gradle build failed! Check build_output.txt for details.
    )
) else (
    echo building a development build
    call gradlew assembleDebug
    if ERRORLEVEL 1 (
        echo Gradle build failed! Check build_output.txt for details.
    )
)

@REM Start ADB server
adb start-server
if ERRORLEVEL 1 (
    echo Failed to start ADB server! Exiting...
)

@REM Check if a device is connected
adb devices
if ERRORLEVEL 1 (
    echo No device found! Make sure your device is connected and USB debugging is enabled.
    adb kill-server
)

@REM Install the APK on the connected device
if "%~1"=="prod" (
    echo Installing a production apk
    adb install ./app/build/outputs/apk/release/app-universal-release.apk
    if ERRORLEVEL 1 (
        echo Gradle build failed! Check build_output.txt for details.
    )
) else (
    echo Installing a dev apk
    adb install ./app/build/outputs/apk/debug/app-universal-debug.apk
    if ERRORLEVEL 1 (
        echo Gradle build failed! Check build_output.txt for details.
    )
)
if ERRORLEVEL 1 (
    echo APK installation failed! Exiting...
    adb kill-server
)

@REM Kill ADB server after installation
adb kill-server
if ERRORLEVEL 1 (
    echo Failed to stop ADB server! Exiting...
)

echo Build and installation completed successfully!

pause

echo "Thanks for using this script!!!"
