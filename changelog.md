# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
We don't strictly follow them. but the exceptions are very less, very intuitive, obvious
and well documented in our release workflow docs.

> [!NOTE]
> This is a raw version of the file..
>
> Final version can be found over
>
at [PasscodesApp/Passcodes-Docs](https://github.com/PasscodesApp/Passcodes-Docs/blob/main/user-docs/changelog.md)

## v1.1.2 - Alpha (Nov 15, 2025)

<details>
  <summary>View Internal Details</summary>

  ```
  Pacakage Name = "com.jeeldobariya.passcodes"
  Min Android = 8.0 (API level 26)
  Max Android = 14 (API level 34)
  Version Code = 3
  Version Name = "v1.1.2-Alpha"
  Master Database Version = "v1"
  ```

</details>

### Breaking Changes

- **Migrate to DataStore**: changed the way how setting and feature flag were been storage
  previously, we use shared preferences but
  now we use datastore which will be more modern way to store such data... but this will also mean
  that your previous setting will be
  not available anymore. we have decide this break changes, because app settings can be restored
  again (in like few minutes). [@JeelDobariya38]

### Added

#### Preview Features

- **Autofill System (Preview Feature)**: added a autofill service to autofill user's credentials on
  the fly. [@hexCode63]

- **Jetpack Compose (Preview Feature)**: added jetpack compose UI & even added a toggle to switch to
  this new UI.
  but it would be worthless to do so, because it just has a single screen.. *this feature is mainly
  for very earlier testing*. [@JeelDobariya38]

### Changed

- **MVI Flavored - Clean Architecture**: migrated the codebase to make it more scalable,
  extendable & readable.
  This will enable us to create better features faster & make a strong base for faster
  iteration. [@JeelDobariya38]

- **Java 21**: migrated the code for passcodes app now compile to java 21. previously it was 
  compiling to java 11. This should improve performance & efficiency a bit. [@JeelDobariya38]

- **Improved Performance**: Improve performance & better responsiveness of UI (less of density
  pixel, more of scalable pixel measurements). [@JeelDobariya38]

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

- **Fixed Import Passwords**: fixed the bug that was not allowing user to select csv files
  from file picker. due to incorrect mimetype in code.. [@JeelDobariya38]

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

- **Improved UI/UX**: improved view password screen's visual feel, also adjust the button colors a
  bit. [@JeelDobariya38]

- **Added Feature Flagging**: given user a control on whether they wanna latest experience or stable
  experience. added a way for launching preview features without worry about their
  stability. [@JeelDobariya38]

- **Update Checking**: made a basic update checker that help users to stay up to date with latest
  release & also notify user about already reported security vulnerability using pre-release
  mechanism. [@JeelDobariya38]

#### Preview Features

- **Copy Button (preview feature)**: added a copy button for copying passwords for easy of use. but
  as it
  is potential threat to security, so made it as a preview feature. [@JeelDobariya38]

- **G-Passwords Import/Export (preview feature)**: added a import/export feature. which is also
  compatible with google passwords. I have test it with my google password setup. but, I am not sure
  weather this will run in every edge case or not. So, it is a preview feature for
  now. [@JeelDobariya38]

### Notes

- **Official Github Organization (Aug 31, 2025)**: migrate project & repository from
  `JeelDobariya38 (personal account)`
  to `PasscodesApp (my organization)` for better development and governance of the project.

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

- **Localized App**: added language translation for English, Chinese, Hindi, Indonesian, Japanese,
  Korean,
  German, Spanish, Vietnamese. [@JeelDobariya38].

- **Improved UI/UX**: added confirmation dialogs for destructive actions, improved support for
  light & dark
  theme with additional minor changes. [@JeelDobariya38 & @kudanilll].

- **New Icon**: rebanded the app as passcodes with a new visual app icon. [@JeelDobariya38].

### Changed

- **Migrated Package Name**: migrate package name from `com.passwordmanager` to
  `com.jeeldobariya.passcodes`. [@JeelDobariya38].

- **Improve Safety By Kotlin Implementation**: move away from `Java` to `Kotlin`
  Language. [@JeelDobariya38].

- **Improve Data Storing Process**: move away from `SqliteDatabase` to `Room` Library for better
  datastorage & security. [@JeelDobariya38].

## v0.1.0 - Alpha (Aug 26, 2024) [YANKED RELEASE]

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

- **App Icon Creation**: designed and implemented the initial app icon, providing the application
  with a recognizable visual identity. [@HamadaNative].

- **Basic App Structure**: established the foundational architecture of the app, including the main
  entry point and initial setup. [@JeelDobariya38].

- **Main Page Development**: developed the main page of the app, including basic UI components and
  initial layout. [@JeelDobariya38].

- **Basic Password Store**: developed a database to store the passwords data. [@kudanilll].

- **Basic Design System**: developed a basic material 2 design system for consistency in app's
  feel & look. [@HamadaNative].

### Notes

- This was the initial alpha pre-release, focusing more on setting up the basic structure and key
  visual
  elements of the app. (`Proof Of Concept`).
