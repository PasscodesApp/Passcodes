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

### Personal details (new)
Besides saved logins, the service can fill a small, deliberately limited set of personal details on forms that aren't sign-in screens (checkout, registration, contact forms):

- first / last / full name, one or more **emails**, one or more **phone numbers** (full, country code, national part), **birth date**, and **address** (full line or street / unit / city / region / postal code / country).
- Suggestions are grouped: one "My details" bundle (default name + email + phone), one per extra email/phone, plus separate **Birth date** and **Address** suggestions. Birth date and address are never mixed into the contact bundle.
- Sign-in screens get saved logins only. Long-press → *Autofill* (a manual request) can still offer details on such a screen.
- **Requires a secure screen lock.** If the device has no PIN/pattern/password/biometric, no personal detail is ever filled. The settings screen and the entry screen both show this.
- Honours the same **authentication** switch as saved logins (locked suggestions show "Tap to unlock"; the values are only released after the prompt, and the unlock intent carries field ids, never the data).
- Address and birth date are not offered on `http://` pages or on pages that mix web origins (e.g. a third-party iframe).
- Values that don't fit a field are **skipped, not truncated** (maxlength, numeric-only inputs, drop-downs that don't contain the exact option, date formats we can't infer).
- **Entered on a native screen, not from React Native.** `AutofillPersonalDetailsActivity` (reached from Autofill settings) is the only place these are written; see below.
- **Stored in its own DataStore** (`passcodes_autofill_information`), not in the Drizzle database. It is local to the device and is **not backed up, imported or exported** by Passcodes. Not encrypted yet: `AutofillInformationStore.encode/decode` is the single place to add that.
- Never logged: the data classes print as `<redacted>`.

### Settings screen (`AutofillSettingsActivity`)
Autofill service status, authentication switch, inline switch, fill-mode selector, light/dark theme, and a "Personal details" card. Open it from React Native with `openAutofillSettings()`.

### Personal details screen (`AutofillPersonalDetailsActivity`)
Reached by tapping the "Personal details" card in settings — not exposed to React Native. A plain Material 3 form: name, birth date (native date picker), a repeatable list of phone numbers and a repeatable list of emails (each with a label, a "default" star and a remove button, capped at 5), and address. Shows the same "no screen lock → won't be filled yet" warning and a "stored on this device only" note at the top. Save writes straight to `AutofillInformationStore` (which validates/trims/caps); "Clear all details" asks for confirmation first.

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

// Personal details: entered on the native settings screen, not from RN.
// These two are read-only — there is no setter in the bridge.
const info = await Module.getAutofillInformation();   // e.g. to show a "configured" badge
await Module.clearAutofillInformation();
```
Settings live in one DataStore file (`passcodes_autofill_settings`) shared by the service, the settings screen and the bridge, so all three always agree. There's no `isDeviceSecure()` here any more — if your own screens need that, check with `expo-local-authentication` instead of duplicating it.

## Code layout

```
PasscodesAutofillServiceModule.kt   JS bridge (AsyncFunctions wrapping runBlocking)
service/       PasscodesAutofillService        fill + save entry points
parser/        AutofillRequestParser, AutofillValues
presentation/  AutofillPresentationFactory (dropdown/inline), AutofillSaveUiFactory (save card)
information/   AutofillInformation (pure model), AutofillInformationStore (DataStore)
identity/      field classifier, form resolver, filler, dataset planner (pure Kotlin)
security/      DeviceSecurity (screen-lock check)
settings/      AutofillPreferences, FillMode, AutofillSettingsActivity, AutofillPersonalDetailsActivity
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
- **No automated tests ship with this module, by project convention** — Passcodes is built and fixed from real usage rather than anticipated test cases. Nothing here has been compiled or run on a device in the environment it was written in; verify on-device before relying on it.

- **Personal-detail detection is heuristic.** It weighs autofill hints, HTML `autocomplete`/`type`, input type and id/label keywords; ambiguous fields ("Name", bare "Country code") are only filled with supporting context. Expect some forms to be missed rather than mis-filled.
- **Native max length isn't visible** to Autofill, so only HTML `maxlength` is respected.
- If you want Android's own cloud backup to skip this data too, exclude `datastore/passcodes_autofill_information.preferences_pb` in your `data_extraction_rules` / `fullBackupContent`:
  ```xml
  <exclude domain="file" path="datastore/passcodes_autofill_information.preferences_pb"/>
  ```

## Future enhancements

Found by reading the code, **not by profiling** — measure on a device before and after each one. Roughly ordered by value within each group.

### Performance (fill latency is what the user feels)
**Done**
- Locked rows no longer read the password column at all (`includePassword = false`; the real password is read by id after unlock).
- One DataStore read per fill request (`AutofillPreferences.snapshot`) instead of three.
- Regexes in the classifier tokenizer, date formatting/parsing and phone-code matching are compiled once.
- Credential matching was already done in SQL (domain / subdomain / package), and `PasscodesDatabase` is a thin wrapper that opens a short-lived connection per call, so neither needed changing.

**Still open**
1. Cache the decoded personal-details record briefly (cleared on `onDestroy`), instead of decoding the JSON on every fill that needs it.
2. Short-circuit the parser: stop walking subtrees once enough fields are found, and skip invisible / not-important-for-autofill nodes earlier.
3. Tokenise each node once and share the tokens between the classifier and the form resolver.
4. Lower the dataset caps (`MAX_DATASETS`, `MAX_IDENTITY_DATASETS`) or build inline specs lazily when the keyboard shows few slots — fewer RemoteViews / Binder bytes per response.
5. Cache `RemoteViews` / icon resources per kind instead of rebuilding for every dataset.
6. Add an index on the normalised domain if the vault grows large, and `LIMIT` the `dumb`-mode query in SQL rather than after loading (`take(MAX_DATASETS)`).
7. Measure first (Perfetto / `adb shell dumpsys autofill`, time-to-first-dataset) — none of the above has been profiled.

### Accessibility
1. Give the dropdown row a real `contentDescription` (e.g. "Passcodes, example.com, Tap to unlock"); the icon is currently `@null`, which is right for the icon but the row text should be announced as one item.
2. Check touch-target size (≥ 48dp) and contrast of every row in light **and** dark with TalkBack on; `contrast.py`-style checks only cover the settings screen.
3. Settings: mark section titles as headings (`accessibilityHeading`), group each switch row so TalkBack reads label + state once, and announce the screen-lock warning with `accessibilityLiveRegion`.
4. Respect font scale: avoid fixed heights in `autofill_item.xml` and the save card; test at 200%.
5. Don't rely on colour alone for the "no screen lock" and "authentication unavailable" states — add an icon + text (partly done).
6. Localise: all strings are in `strings.xml` already; add translations and RTL checks (`start`/`end` everywhere).

### Code quality
No automated tests, by design (see "Quirks and limitations") — these are about the code's structure, not about adding a test suite.
1. **Split `PasscodesAutofillService.kt` (~540 lines):** move dataset building into a `DatasetBuilder` class and request/response policy into a small pure `FillPolicy`, leaving the service as plumbing.
2. **Replace stringly-typed intent extras** with one typed `UnlockRequest` (parcelable/serialisable) shared by service and activity.
3. **Single source for the partition rules** (`identity` vs credential vs save) so the "never mix partitions" invariant is asserted in one place.
4. **Structured logging with a redaction wrapper** so a future `Log.d` cannot print form content; add a lint/ktlint rule banning `Log` calls in `identity/` and `information/`.
5. **Version and migrate the stored record** (`information_v1` → v2) with an explicit migrator, ahead of citizen-card / passport fields.
6. **Lint/detekt + `-Werror`** for the module once the SDK build is wired up; remove unused imports as the code moves.
7. **`AutofillPersonalDetailsActivity` is one largish Activity** (view binding, row inflation and persistence all in one place); split row management (phones/emails) into a small adapter-like helper if it grows further.

### Security / privacy
1. **Encrypt the details** with an Android Keystore key (AES-GCM) in `AutofillInformationStore.encode/decode`; optionally require user-auth-bound keys.
2. **Per-site consent for sensitive fields:** remember "never offer address / birth date on this domain".
3. **Verify web origin against Digital Asset Links** before offering personal details to a native app.
4. **Auto-lock the cache:** clear any in-memory copy of the details when the service is destroyed.
5. Exclude the DataStore from cloud backup explicitly (snippet above) rather than relying on app-level defaults.

### Features / accuracy
1. Learn from corrections: when a user edits a filled field, optionally record "this id means X" for that form (local only).
2. More field kinds (national ID, passport, tax ID) behind the same partition model, each in its own dataset, with stricter confidence and mandatory authentication.
3. More locales for address ordering, phone formats and date patterns; per-country postal-code validation.
4. Support `AutofillValue.forToggle` fields (e.g. gender, consent checkboxes) only if the user opts in.
5. In-app "test my details" screen that shows what each stored value would look like in a typical form.

## Dependencies

`passcodes-native-database-service` (Gradle: `implementation project(':passcodes-native-database-service')`), DataStore Preferences, AndroidX Biometric, AndroidX Autofill, Material 3, kotlinx-coroutines (core + android), lifecycle-runtime-ktx.

## What changed in the recent rework

- Autofill code split into sub-packages; all SQLite code moved out into the database module.
- Added schema awareness (`application_ids`, `source`, soft-delete via `deleted_at`).
- Added biometric unlock flow, inline-suggestion toggle, Smart/Auto/Dumb fill modes, dark mode and restyled UI, custom Save card.
- Added personal-details autofill (name, phone, email, birth date, address) behind the same screen-lock and authentication gates as saved logins.
- Service-side read/write paths use coroutines (`lifecycleScope` in Activities, a service-owned scope in the service); the JS bridge (`PasscodesAutofillServiceModule`) uses `AsyncFunction` + `runBlocking(Dispatchers.IO)`, by project choice.
- Robustness: empty `FillResponse` no longer throws; unexpected exceptions in a request are caught instead of crashing the app process.
- Personal details are entered entirely on a native screen (`AutofillPersonalDetailsActivity`, opened from Autofill settings) — there's no React Native entry point or bridge setter for them, and `isDeviceSecure()` was removed from the bridge (use `expo-local-authentication` from RN if you need that check).
- No automated tests ship with this module — see "Quirks and limitations".
