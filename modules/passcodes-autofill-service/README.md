# passcodes-autofill-service

Expo module that makes Passcodes an **Android Autofill provider** (Android 8.0 / API 26+).
It fills saved logins into apps and browsers, and offers to save new ones.
All database access goes through [`passcodes-native-database-service`](../passcodes-native-database-service).

## Features

### Filling
- Detects username / password / new-password fields from the screen structure and offers saved credentials.
- **Three presentation tiers**, picked automatically: dropdown row (API 26+), inline keyboard chip (API 30+), and the `Presentations` API (API 34+).
- **Fill mode** (user setting, shared with React Native):
  | Mode | Behaviour |
  |---|---|
  | `smart` | Only credentials matching the site/app. No match → nothing shown. |
  | `auto` (default) | Matching credentials first; if none match, everything. |
  | `dumb` | Always everything. |
- **Biometric / device-credential lock** (user setting, **off by default**). When on, suggestions are "Tap to unlock" rows; real values are read from the DB only after `BiometricPrompt` succeeds. If the device has no screen lock or biometrics enrolled, the lock is skipped rather than locking the user out.
- **Inline suggestions toggle** (default on). Works with the lock: a locked chip shows the site name and "Tap to unlock", never the username. The settings screen recommends turning inline off when the lock is turned on — a recommendation only, the user decides.

### Saving
- Offers "Save password?" after a login form is submitted, with a custom card (site/app name, username, masked password; API 27+, plain system text on 8.0).
- Saving upserts by **(normalized domain, username)**: updates the password of an existing row, otherwise inserts a new one.
- Web form → `domain` is the web domain, no `application_ids` change. Native app form → `domain` is the app's label and the package name is appended to `application_ids` (comma-separated, de-duplicated).
- Rows written this way get `source = AUTOFILL` and a note saying they need review. An existing real `url` is never overwritten.

### Settings screen (`AutofillSettingsActivity`)
Autofill service status, authentication switch, inline switch, fill-mode selector, light/dark theme. Open it from React Native with `openAutofillSettings()`.

## Using it from React Native

```ts
import Module from "./modules/passcodes-autofill-service/src/PasscodesAutofillServiceModule";

Module.isAutofillServiceEnabled();            // boolean (sync)
Module.openAutofillSettings();                // opens the native settings screen

await Module.isInlineSuggestionsEnabled();    // boolean
await Module.setInlineSuggestionsEnabled(true);   // resolves to the value read back
await Module.isBiometricLockEnabled();        // boolean
await Module.setBiometricLockEnabled(true);
await Module.getFillMode();                   // "smart" | "auto" | "dumb"
await Module.setFillMode("smart");
```
Settings live in one DataStore file (`passcodes_autofill_settings`) shared by the service, the settings screen and the bridge, so all three always agree.

## Code layout

```
PasscodesAutofillServiceModule.kt   JS bridge (suspend AsyncFunctions)
service/       PasscodesAutofillService        fill + save entry points
parser/        AutofillRequestParser, AutofillValues
presentation/  AutofillPresentationFactory (dropdown/inline), AutofillSaveUiFactory (save card)
settings/      AutofillPreferences, FillMode, AutofillSettingsActivity
unlock/        AutofillUnlockActivity          transparent host for BiometricPrompt
```

## Quirks and limitations

- **Concurrency:** requests run on a coroutine scope, one at a time. A system cancel cancels the request; destroying the service cancels fills but **not** an in-progress save (so a typed password isn't lost).
- **At most 20 suggestions** are sent (most recently updated first) — datasets cross Binder, which has a ~1 MB limit. This mostly matters in `dumb` mode.
- **`smart` mode matches the web domain / package only.** A site saved under a different host than the one the page reports can be missed; `auto` is the safer default. Domain matching is one-way: stored `google.com` covers `accounts.google.com`, not the reverse.
- **Inline chips depend on the keyboard.** The IME must support inline suggestions, and it styles the chip itself (we only supply text and a tinted icon).
- **Save-failure feedback is not custom:** if a save fails, the system shows its generic message. No toast/notification of our own yet.
- **The Save card is built when the fill response is built**, so it can't tell "new login" from "update existing" and can't show the username before the user types it — the username is copied from the screen when the prompt appears. An empty username shows "—".
- **Dropdown/save RemoteViews use explicit colour resources** (with `values-night` variants), not theme attributes — the system inflates them in another process. Only RemoteViews-safe widgets are allowed in those layouts (no plain `View`).
- Passwords are read/written exactly as stored in the `password` column — this module adds no encryption layer.
- Not yet verified on a range of real devices/keyboards/browsers; test on Chrome, a native app, with and without the lock, and with inline on and off.

## Dependencies

`passcodes-native-database-service` (Gradle: `implementation project(':passcodes-native-database-service')`), DataStore Preferences, AndroidX Biometric, AndroidX Autofill, Material 3, kotlinx-coroutines (core + android), lifecycle-runtime-ktx.

## What changed in the recent rework

- Autofill code split into sub-packages; all SQLite code moved out into the database module.
- Added schema awareness (`application_ids`, `source`, soft-delete via `deleted_at`).
- Added biometric unlock flow, inline-suggestion toggle, Smart/Auto/Dumb fill modes, dark mode and restyled UI, custom Save card.
- Replaced `Executors`/`Thread`/`runBlocking` with coroutines (`lifecycleScope` in Activities, a service-owned scope in the service, `Coroutine` AsyncFunctions in the bridge).
- Robustness: empty `FillResponse` no longer throws; unexpected exceptions in a request are caught instead of crashing the app process.
