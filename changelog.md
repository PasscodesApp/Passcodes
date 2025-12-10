# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Release Notes: [docs/release-notes.md](docs/release-notes.md), there you will find short and sweet
release notes.

> [!NOTE]
> This is a raw version of the file..
>
> Final version is over
> on [PasscodesApp/Passcodes-Docs](https://github.com/PasscodesApp/Passcodes-Docs/blob/main/user-docs/changelog.md)

## v1.1.1 - Alpha (Sept 11, 2025)

<details>
  <summary>View Internal Details</summary>

  ```
  Pacakage Name = "com.jeeldobariya.passcodes"
  Min Android = 8.0 (API level 26)
  Max Android = 14 (API level 34)
  Version Code = 2
  Version Name = "v1.1.1-Alpha"
  Master Database Version = "v1"
  ```

</details>

### Fixed

- **Fixed Import Passwords**: fix the bug, that was not let user select csv files from file picker.
  due to incorrect mimetype in code.. Contributed by [@JeelDobariya]

## v1.1.0 - Alpha (Sept 1, 2025)

<details>
  <summary>View Internal Details</summary>

  ```
  Pacakage Name = "com.jeeldobariya.passcodes"
  Min Android = 8.0 (API level 26)
  Max Android = 14 (API level 34)
  Version Code = 2
  Version Name = "v1.1.0-Alpha"
  Master Database Version = "v1"
  ```

</details>

### Added

- **Improve UI/UX**: improve view password screen, also adjust the button colors.. Contributed
  by [@JeelDobariya]
- **Add Feature Flagging**: give user a control on whether they wanna latest experience or stable
  experience. add a way for launching preview features without worry about their stability.
  Contributed by [@JeelDobariya]
- **Update Checker**: made a basic update checker that help users stay out to date with latest
  release & also notify user about already reported security vulnerability. Contributed
  by [@JeelDobariya]
- **Copy Button (preview feature)**: add copy button for copy passwords for easy of use. but as it
  is potential threat to security, made it a preview feature. Contributed by [@JeelDobariya]
- **G-Passwords Import/Export (preview feature)**: add a import/export feature. which also
  compatible with google passwords. I have test it with my google password setup. but, I am not sure
  wether this will run in every edge case or not. So, it a preview feature. Contributed
  by [@JeelDobariya]

### Changed

- **Migrated Project**: Migrate project from `JeelDobariya38 (personal)` to
  `PasscodesApp (my organization)` account. To reflect my long term vision. Contributed
  by [@JeelDobariya].

## v1.0.0 - Stable (Aug 16, 2025)

<details>
  <summary>View Internal Details</summary>

  ```
  Pacakage Name = "com.jeeldobariya.passcodes"
  Min Android = 8.0 (API level 26)
  Max Android = 14 (API level 34)
  Version Code = 1
  Version Name = "v1.0.0-Stable"
  Master Database Version = "v1"
  ```

</details>

### Added

- **Localized App**: add language support for English, Chinese, Hindi, Indonesian, Japanese, Korean,
  German, Spanish, Vietnamese. Contributed by [@JeelDobariya].
- **Improved UI/UX**: add confirmation dialogs, support for light & dark theme with additional minor
  changes. Contributed by [@JeelDobariya & @kudanill].
- **New Icon**: add new icons to app. Contributed by [@JeelDobariya].

### Changed

- **Migrated Package Name**: Migrate package name from `com.passwordmanager` to
  `com.jeeldobariya.passcodes`. Contributed by [@JeelDobariya].
- **Improve Safety By Kotlin Implementation**: Move from `Java` to `Kotlin` Language. Contributed
  by [@JeelDobariya].
- **Improve Data Storing Process**: Move from `SqliteDatabase` to `Room` Library for better
  datastorage. Contributed by [@JeelDobariya].

## v0.1.0 - Alpha (Aug 26, 2024)

<details>
  <summary>View Internal Details</summary>

  ```
  Pacakage Name = "com.passwordmanager"
  Min Android = 8.0 (API level 26)
  Max Android = 13 (API level 33)
  Version Code = 1
  Version Name = "0.1.0-Alpha"
  Master Database Version = "v1"
  ```

</details>

### Added

- **App Icon Creation**: Designed and implemented the initial app icon, providing the application
  with a recognizable visual identity. Contributed by [@HamadaNative].
- **Basic App Structure**: Established the foundational architecture of the app, including the main
  entry point and initial setup. Contributed by [@JeelDobariya].
- **Main Page Development**: Developed the main page of the app, including basic UI components and
  initial layout. Contributed by [@HamadaNative].

### Notes

- This is the initial alpha release, focused on setting up the basic structure and key visual
  elements of the app.
