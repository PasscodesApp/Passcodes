# passcodes-native-database-service

Expo module holding **all native (Kotlin) access to the Passcodes SQLite database**. The Autofill module uses it today; a future importer/exporter should too, so nothing else needs to know about SQLite.

It reads and writes the file Expo SQLite / Drizzle already manages: `<filesDir>/SQLite/master.db`.
It **never creates the file and never runs migrations** — the React Native side owns the schema. If the file doesn't exist yet, reads return empty/zero and writes return `null`.

## Using it from Kotlin

Add to the consuming module's `android/build.gradle`:
```gradle
implementation project(':passcodes-native-database-service')
```
(The name after the colon is the Gradle project name autolinking gives this module; if it isn't found, run `npx expo-modules-autolinking resolve --platform android`.)

```kotlin
val db = PasscodesDatabase(context)   // cheap; opens a connection per call
```

### Public API — `com.jeeldobariya.passcodes.database.PasscodesDatabase`

| Function | Returns | Notes |
|---|---|---|
| `getAllPasswords()` | `List<PasswordEntry>` | All non-deleted rows, most recently updated first. |
| `filterPasswords(webDomain, packageName, fallbackToAll = false)` | `List<PasswordEntry>` | Rows for a site and/or app. See matching rules below. |
| `getPasswordById(id)` | `PasswordEntry?` | `null` if missing or soft-deleted. |
| `countPasswords()` | `Int` | Non-deleted rows. |
| `savePassword(domain, username, password, url = null, packageId = null)` | `Long?` | Upsert; row id, or `null` on failure. |

**Rules for every call**
- **Blocking file I/O** — call from a background thread (`Dispatchers.IO`), never the main thread.
- **Never throws** — failures are logged (`PasscodesDatabase` tag) and come back as empty / `null` / `0`.
- Soft-deleted rows (`deleted_at IS NOT NULL`) are always excluded.
- Each write runs in one transaction; an exception rolls it back.

**`filterPasswords` matching**
- `webDomain` is normalized (scheme, `www.`, path, port, trailing dot stripped; lowercased) and matches the `domain` column **exactly or as a subdomain** of a stored domain (stored `google.com` matches `accounts.google.com`; not the reverse).
- `packageName` matches one **exact segment** of the comma-separated `application_ids` column, case-insensitively. It is never compared to `domain`.
- With neither identifier, or no match, you get an empty list — unless `fallbackToAll = true`, which returns everything instead.

**`savePassword` behaviour**
- Blank password → `null`. `domain` is normalized (falls back to the trimmed/lowercased input).
- Matches an existing row by **(domain, username)**; if duplicates exist, the most recently updated wins.
  - Existing row: updates `password`, `notes`, `application_ids`, `updated_at`; keeps an existing non-blank `url`.
  - New row: inserts with `source = 'AUTOFILL'`, a "needs user review" note, and `url` = given URL or `https://local.<domain>`.
- `packageId`: pass the app's package name **only for native-app forms**; it's appended to `application_ids` without duplicates (`"com.outlook.ios,com.outlook.android"`). Pass `null` for web forms.

### Model — `PasswordEntry`
`id`, `domain`, `username`, `password`, `notes?`, `url?`, `applicationIds?`, `source?` (`IMPORTED | APPLICATION | AUTOFILL`), `createdAt`, `updatedAt` (epoch seconds).

### Helpers — `database.util`
- `DomainHelper.normalizeDomain(raw)`, `DomainHelper.buildFallbackUrl(domain)`
- `AppIdentifierHelper.parse(raw)`, `.merge(existing, identifier)`, `.containsSqlFragment(column)`

## Using it from React Native

```ts
import Module from "./modules/passcodes-native-database-service/src/PasscodesNativeDatabaseModule";
const count = await Module.getPasswordCount();   // 0 on web or if the DB doesn't exist
```
Deliberately tiny: credentials shouldn't cross the JS bridge more than necessary. Import/export entry points will be added to `PasscodesNativeDatabaseModule.kt`.

## Code layout

```
PasscodesDatabase.kt              public facade (the only class other modules use)
PasscodesNativeDatabaseModule.kt  JS bridge
model/       PasswordEntry
schema/      PasswordsSchema      table/column names, source value, note markers (mirror of the Drizzle schema)
storage/     DatabaseConnection   DB file location, read-only / read-write open
repository/  PasswordReader, PasswordWriter
util/        DomainHelper, AppIdentifierHelper
```
`schema`, `storage` and `repository` are `internal`: other modules can't depend on them, so they can be reworked freely.

## If the schema changes

Edit `schema/PasswordsSchema.kt` (names) and the reader/writer. The DB path is the one contract with Expo SQLite — `DATABASE_PATH` in `storage/DatabaseConnection.kt`.

## Quirks

- No `busy_timeout` PRAGMA is set: Android already applies its own default wait (~2.5 s) when the RN side holds a write lock.
- Passwords are read and written exactly as stored — no encryption layer here.
- A new connection is opened per call (simple and safe; fine at autofill frequency, revisit for bulk import).
- `application_ids` and `domain` matching are done in SQL (`instr`/`substr`, not `LIKE`), so `_` in package names isn't treated as a wildcard.
- Only autofill-style writes exist so far; there is no delete/bulk API yet.
