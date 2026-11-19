# AGENT.md

Guide for AI coding agents working in the **Passcodes** repository. Read this
before changing anything. It tells you what the project is, where things live,
which rules are non-negotiable and how to make a change safely.

- Business rules (what the app must do, with source files): [`Business rules.md`](./Business%20rules.md)
- Privacy policy (what data is handled and what can leave the device): [`PRIVACY.md`](./PRIVACY.md). Update it whenever you add a network call, SDK, permission, data type, backup or encryption change.
- Human docs: [`README.md`](./README.md), [`building.md`](./building.md), [`SECURITY.md`](./SECURITY.md) and the external
  [Passcodes-Docs](https://passcodesapp.github.io/Passcodes-Docs) site.

Everything below was derived from the code in this repository. If code and this
file disagree, **the code wins**: fix this file in the same PR.

---

## 1. Project snapshot

| | |
|---|---|
| What it is | Open-source, local-first password manager. No cloud, no accounts. |
| Status | BETA. **Data is stored unencrypted** (see README warning). Encryption is planned, not implemented. |
| Version | `src/config.ts` -> `VERSION` / `BUILD_VERSION` (`v4.0.1 - STABLE RC.3`), `BUILD_ANDROID_VERSIONCODE` (`11`) |
| Platforms | Android is primary (the Kotlin autofill and database modules are Android-only). iOS is configured in `app.config.ts`. Web is only scaffolded (see 12). |
| Package id | `com.jeeldobariya.passcodes` (+ `.dev` / `.preview` suffix for non-production variants) |
| Database | SQLite file `master.db` (`Config.DATABASE_NAME`), accessed through Drizzle ORM + `expo-sqlite` |
| License | MIT |

## 2. Non-negotiable rules for agents

1. **Never log, print, toast or put into an error message any password, username, note, URL, imported value or personal detail.** Existing code uses fixed text and counts only (see `FAILURE_REASON_TEXT`, import/export outcomes). Keep it that way.
2. **Never weaken a security default.** App Lock and screenshot prevention default to ON; reading a missing or corrupt flag must fall back to the default (`features/biometrics/flag.ts`).
3. **Do not add network calls that carry user data.** The only network traffic is website-icon downloads, and only the host name goes to the chosen provider (Settings > Website icons, can be "Off").
4. **Never silently destroy user data.** Deletes are soft (`deleted_at`); permanent deletion only works on rows already in "Recently deleted". Imports never overwrite; conflicts are decided by the user. Preserve this pattern.
5. **Do not modify the user's existing database file in recovery paths.** `DatabaseRecoveryScreen` / `db/exportDatabase.ts` only read and copy; connections are deliberately never closed (closing the last WAL connection would merge/delete `-wal`).
6. **Do not rename persisted keys or columns without a migration.** Notably the setting key `feat_auth_isBiometicsEnabled` keeps its typo on purpose, and `Config.OLD_MISTAKEN_DATABASE_NAME` (`test2.db`) must never be edited.
7. **Schema changes must be done in three places**: `src/db/schema.ts`, a generated Drizzle migration, and the Kotlin mirror `modules/passcodes-native-database-service/.../schema/PasswordsSchema.kt` (plus reader/writer if columns are used by autofill).
8. **Do not add a test suite, test runner or test files unless the maintainer asks.** The project builds and fixes from real usage; no test script exists in `package.json` and the native module README states "no automated tests, by project convention".
9. **Vulnerabilities are reported privately** (see `SECURITY.md`). Never put exploit details in a public issue, PR title or PR description.
10. Keep changes small and focused. Ask before large refactors, new dependencies or new native code.

## 3. Tech stack

| Area | Choice |
|---|---|
| Framework | Expo SDK `~58`, React Native `0.88.1`, React `19.3.0`, **React Compiler enabled** (`experiments.reactCompiler`) |
| Language | TypeScript `~6.0.3`, very strict (see 8), Kotlin for native Android |
| Routing | `expo-router` `~58`, file-based, **typed routes** on (`experiments.typedRoutes`) |
| UI | `react-native-paper` `6.0.0-alpha.2` (Material), FontAwesome 6 icons (`@react-native-vector-icons/fontawesome6/static`), `@shopify/flash-list` 2.x for long lists, `react-native-reanimated` 4.x |
| Data | `drizzle-orm` `^0.45` + `expo-sqlite` (live queries via `useLiveQuery`), `drizzle-kit` for migrations |
| Key-value settings | `expo-sqlite/kv-store` (sync reads, async writes) |
| Security | `expo-local-authentication` (biometrics / device credential), `expo-screen-capture`, `react-native-quick-crypto` (secure random) |
| Files | `@react-native-documents/picker` (open / save-as), `expo-file-system`, `papaparse` (CSV) |
| Shared logic | `@passcodes/passalgo` (git dependency, tag `v1.0.0`): password generator, strength score, `formatTimestamp` |
| Native | Two local Expo modules in `modules/` (Kotlin), one Expo config plugin in `plugins/` |
| CI | GitHub CodeQL only (`.github/workflows/codeql.yml`: JS/TS always; Kotlin when `modules/**` or `plugins/**` change). No lint/test workflow. |

## 4. Commands

Package manager is **npm** (`package-lock.json`, `.npmrc` present).

```bash
npm install              # install deps
npm start                # expo start (Expo Go works only for non-native features)
npm run android          # expo run:android (dev build on device/emulator)
npm run lint             # expo lint  (eslint-config-expo flat config)
npx tsc --noEmit         # type-check (no script defined; run it directly)
npx expo-doctor          # recommended after dependency changes

npm run db:generate -- <migration_name>   # drizzle-kit generate --name <migration_name>

npm run build:develop    # local EAS dev build (APP_VARIANT=development)
npm run build:preview    # local EAS preview build
npm run deploy           # local EAS production (universal APK)
npm run deploy:arm64 | deploy:armv7 | deploy:x86 | deploy:x86_64   # per-ABI production builds
npm run build            # scripts/build-all-cloud.sh: submit all 5 production builds to EAS cloud
```

There is **no `npm test`**. Before opening a PR run at least `npm run lint` and `npx tsc --noEmit`.

Native code (anything in `modules/` or `plugins/`) does not run in Expo Go; it needs a development build.

## 5. Repository map

```
app.config.ts              Expo config. Build variants (development/preview/production), ids, plugins
eas.json                   EAS build profiles (development, preview, production, deploy, deploy-<abi>)
drizzle.config.ts          drizzle-kit: schema -> src/db/drizzle, dialect sqlite, driver expo
babel.config.js            inline-import plugin for .sql (needed by Drizzle migrations)
metro.config.js            adds "sql" to sourceExts
tsconfig.json              strict TS + path aliases  @/* -> src/*   @plugins/* -> plugins/*
eslint.config.js           eslint-config-expo/flat
building.md                how to build a production APK with EAS
scripts/                   build-all-cloud.sh
plugins/withProtectUserData/   config plugin: android:allowBackup=false + hasFragileUserData=true
modules/
  passcodes-native-database-service/   Kotlin: ALL native SQLite access (shared core)
  passcodes-autofill-service/          Kotlin: Android Autofill provider (logins + personal details)
src/
  config.ts                app name, version, build numbers, DB name, social links
  app/                     expo-router routes (see 6.3)
  components/              reusable UI (Screen, SecureTextField, PasswordItemCard, lists, FABs, dialogs, ...)
  contexts/                RepositoryContext (repos), ToastContext (toasts)
  db/                      schema.ts, provider.tsx (open+migrate), drizzle/ (SQL + snapshots + migrations.js),
                           exportDatabase.ts (raw .db export for recovery)
  repositories/            PasswordRepository, CategoryRepository, passwordChanges (pure)
  features/                self-contained feature folders (see 7)
  libs/                    small shared helpers/stores (see 7.9)
.github/                   CodeQL workflow, issue templates, PR template, release-notes categories
```

## 6. Architecture

### 6.1 Start-up sequence

`src/app/_layout.tsx`:

```
ThemeProvider (light/dark from system)
 -> PaperProvider
   -> RepositoryProvider(dbName = "master.db")
        -> DrizzleDatabaseProvider (src/db/provider.tsx)
             opens DB (openDatabaseSync, WAL change listener on)
             -> runs Drizzle migrations (useMigrations)
             -> on any failure: <DatabaseRecoveryScreen/> instead of the app
             -> on success: provides db via context
        -> AppContent
             locked?  -> <LockScreen/>   (features/biometrics)
             else     -> ToastProvider -> <Slot/> (routes)
```

Children never render before the DB is migrated. "Try again" restarts the whole
start-up (keyed remount) and reuses the already-open connection.

### 6.2 Layering

```
Screens / components  (src/app, src/components, src/features/*/screens)
        |  call hooks: usePasswordRepository(), useCategoryRepository(), useToast()
        v
Repositories          (src/repositories)  only place that builds Drizzle queries for passwords/categories
        |
        v
Drizzle + expo-sqlite (src/db)            schema.ts is the single schema definition on the JS side
        ^
        |  same file, separate connection
Kotlin PasscodesDatabase (modules/passcodes-native-database-service)   used by the autofill service
```

- Screens read lists with `useLiveQuery(repo.observeX(), deps)` (Drizzle live queries). Writes go through repository methods.
- Repository methods that need atomicity use `db.transaction` and batch IDs in chunks (`BATCH_SIZE = 500`, import chunks of 200) because SQLite limits bound variables.
- The SQLite driver is synchronous; long loops yield with `setTimeout(0)` (see `importMany`).

### 6.3 Routes (`src/app`)

```
_layout.tsx                         root (providers, lock)
(tabs)/_layout.tsx                  bottom tabs declared: "Passcodes" (house-user), "Settings" (gear)
(tabs)/password-generator.tsx       NOT declared in Tabs.Screen; expo-router may still expose it as an extra tab (verify on device)
(tabs)/(password-manager)/          Stack
    index.tsx                       home: grouped list OR folder grid, search, multi-select, FAB
    save-password.tsx               "New Password" form sheet (params: categoryId, lockCategory)
    password-details.tsx            view/edit one password (param: id)
    category-passwords.tsx          passwords of one folder (param: categoryId or "uncategorized")
(tabs)/(settings)/                  Stack
    settings.tsx                    all settings + recovery + about
    password-generator.tsx          generator (opened from Settings; a second copy exists at (tabs)/password-generator.tsx)
    recently-deleted.tsx            restore / delete forever
    import-passwords.tsx, import-review.tsx, export-passwords.tsx
    get-back-passwords(.android).tsx  one-off migration of the old Room (v1/v2 native app) database
    data-recovery.tsx               recovers rows from the old mistaken "test2.db"
```

Routes are typed: when adding a route, check `Href` usage and `router.push/navigate` calls still type-check.

### 6.4 State outside React

Several small stores live outside React state and are read with `useSyncExternalStore`
so only the affected components re-render:

| Store | File | Purpose |
|---|---|---|
| Multi-select | `libs/selection_store.ts` | selected password ids, per-card state, Android back leaves selection first |
| Home layout | `libs/home_layout.ts` | `"grouped"` (default) or `"folders"`, key `feat_home_layout` |
| Favicon provider | `features/favicons/setting.ts` | key `feat_favicon_provider` (legacy `feat_website_icons_isEnabled`) |
| App lock state machine | `features/biometrics/lockController.ts` | locked / authenticating / last outcome |
| Import session | `features/import-export/importSession.ts` | parsed file + plan between pick and review (plaintext, memory only) |

### 6.5 Persisted settings (kv-store keys)

| Key | Meaning | Default |
|---|---|---|
| `feat_auth_isBiometicsEnabled` | In-app lock (**typo is intentional, do not rename**) | `true` |
| `feat_screenshot_prevention_isEnabled` | Block screen capture while a password is revealed | `true` |
| `feat_home_layout` | `grouped` / `folders` | `grouped` |
| `feat_favicon_provider` | `google` / `duckduckgo` / `faviconim` / `off` | `google` |
| `favicon_meta_<host>` | favicon cache metadata `{at, ok, provider}` | - |
| `room_expo_migration_complete_v1` | state of the old-Room-database migration | - |

## 7. Features and where they live

### 7.1 Password manager (core)
`src/app/(tabs)/(password-manager)/*`, `src/components/Password*`, `src/repositories/PasswordRepository.ts`.
Create / view / edit / soft-delete, search, multi-select (move, delete), copy actions, website icons.

### 7.2 Categories (folders)
`src/repositories/CategoryRepository.ts`, `src/libs/category_groups.ts`, `src/libs/category_actions.tsx`,
`src/components/Category*`, `Folder*`, `GroupedPasswordList.tsx`, `CategorySection.tsx`.
Flat categories; "Uncategorized" is a virtual group (`category_id IS NULL`). Two home layouts share one data hook (`useCategoryGroups`) so they never disagree. The UI word is "folder" or "category" depending on layout (`CategoryNoun`).

### 7.3 Recently deleted
`src/features/recently-deleted/`. Restore / delete forever, one or all.

### 7.4 Import / export
`src/features/import-export/`. See `Business rules.md` section on import/export.

```
types.ts                  shared types, TRANSFER_FORMAT_OPTIONS, FAILURE_REASON_TEXT
formats/googleCsv.ts      parse + serialize Google Password Manager CSV (pure)
formats/passcodesJson.ts  parse + serialize Passcodes JSON backup (pure)
utils/duplicates.ts       duplicate / conflict analysis (pure)
services/importService.ts prepareImport() then commitImport() (pure workflow, repos injected)
services/exportService.ts exportPasswords() (pure workflow, repos + save fn injected)
files.ts                  native file picker / Save As glue (the services receive it as an injected function)
importSession.ts          in-memory session between screens
screens/, components/     UI
```

Pattern to preserve: format, analysis and service modules import **no React Native / Expo code** (dependencies are injected) so they stay runnable in plain Node.

### 7.5 Security features
- `src/features/biometrics/` App Lock. `authentication.ts` (pure, fail-closed), `lockController.ts` (pure state machine, 2 min auto-lock), `flag.ts` (pure persisted switch), `runtime.ts` (the only file importing native modules), `useAppLock.ts`, `components/LockScreen.tsx`. `confirmIdentity(reason)` is the shared "prove it's you" prompt for sensitive actions.
- `src/features/screenshot-prevention/` blocks capture while a password is revealed; used through `components/SecureTextField.tsx` (requires a unique `screenCaptureKey`).
- `plugins/withProtectUserData` sets `allowBackup=false` and `hasFragileUserData=true` (TODO in `app.config.ts`: allow backup once encryption exists).

### 7.6 Password generator
`src/features/password-generator/`. Options + strength come from `@passcodes/passalgo`; randomness is injected (`quickCryptoRandom` from `react-native-quick-crypto`). Length 4-64, default 16, default charset upper+lower.

### 7.7 Website icons
`src/features/favicons/`. Provider list + URL builder + host validation (`providers.ts`), persistent file cache with refresh rules (`cache.ts`), setting store, hook, provider dialog.

### 7.8 Database recovery
`src/features/database-recovery/DatabaseRecoveryScreen.tsx` + `src/db/exportDatabase.ts`. Plain-language, read-only screen shown when the DB can't be opened/migrated; offers raw `.db` export (after identity check, with a warning) and community links. It deliberately uses no app providers (rendered before they exist).

### 7.9 Shared helpers (`src/libs`)
`application_ids.ts` (parse/validate package names), `timestamp_unixepoch_utils.ts` (epoch-seconds helpers), `safe_area.ts` (the single place to tune safe-area), `withSQLiteDatabase.ts`, `room_drizzle_migration.ts` + `data_recovery_test_db_mess.ts` (legacy data recovery), selection/home-layout stores, category hooks.

### 7.10 Safe area
Every screen should render inside `components/Screen.tsx`, which reads `libs/safe_area.ts`. Change numbers there, not per screen. (A few older screens still use `SafeAreaView` directly.)

## 8. Code conventions

- **Imports**: use the `@/` alias for anything under `src/`. Relative paths only inside a feature folder.
- **TypeScript flags** (`tsconfig.json`): `strict`, `noUncheckedIndexedAccess`, `exactOptionalPropertyTypes`, `noImplicitReturns`, `noImplicitOverride`, `noPropertyAccessFromIndexSignature`. Practical consequences: array/index access may be `undefined`; do not pass `undefined` explicitly to an optional property; read index-signature objects with `obj["key"]`.
- **Pure vs. native**: put logic in modules that import no RN/Expo code; put platform glue in a `runtime.ts` / `files.ts` style file. New features should follow this split.
- **Naming**: components `PascalCase.tsx`; `libs/` helpers `snake_case.ts`; feature internals `camelCase.ts`; screen content that is reused lives in `features/<x>/ScreenContent` and the route file only wraps it.
- **Timestamps** are Unix epoch **seconds** (integers, UTC) everywhere in the DB and in the JSON backup. Use `CURRENT_TIMESTAMP_SQL` via `sql.raw(...)` when the database must evaluate "now" (do not bind it as text).
- **Errors shown to users** are short, fixed strings. Repository validation errors use `CategoryValidationError` whose message is safe for a toast.
- **Toasts**: `useToast().showToast(message, "success" | "error")` (default 2200 ms).
- **Destructive UI**: confirm with `Alert.alert`, mark the button `style: "destructive"`, put the safe option first (see `libs/category_actions.tsx`).
- **Lists**: use `FlashList` for long lists; keep per-item state in external stores to avoid re-rendering the whole list.
- **Comments**: this codebase documents *why* (security reasoning, SQLite quirks) at the top of files and next to non-obvious code. Match that style.
- **UI copy**: plain language, no jargon; warn explicitly when something is unencrypted or irreversible.

## 9. Data model

Defined in `src/db/schema.ts` (Drizzle). Timestamps = epoch seconds.

`categories`

| column | type | notes |
|---|---|---|
| `id` | integer PK autoincrement | |
| `name` | text NOT NULL | unique case-insensitively (enforced in `CategoryRepository`, not by a DB constraint) |
| `created_at`, `updated_at` | integer NOT NULL | default now; `updated_at` refreshed on update |

`passwords`

| column | type | notes |
|---|---|---|
| `id` | integer PK autoincrement | |
| `domain` | text NOT NULL | display name / site (matched case-insensitively on import) |
| `username` | text NOT NULL | |
| `password` | text NOT NULL | **plaintext today** |
| `notes`, `url` | text NULL | |
| `created_at`, `updated_at` | integer NOT NULL | default now |
| `deleted_at` | integer NULL | `NULL` = active, set = in Recently deleted (soft delete) |
| `application_ids` | text NULL | comma-separated Android package names / iOS bundle ids |
| `source` | text enum `IMPORTED` \| `APPLICATION` \| `AUTOFILL` NULL | where the row came from (NULL for legacy / migrated rows) |
| `category_id` | integer NULL FK -> `categories.id` `ON DELETE SET NULL` | `NULL` = Uncategorized; index `passwords_category_id_idx` |

Migrations live in `src/db/drizzle/` (`0000_init_after_migrating_from_room_android`, `0001_v2_unixtimestamp_softdelete_applictionids`), with `meta/_journal.json` and the generated `migrations.js`. Never hand-edit generated snapshot/journal files; run `npm run db:generate -- <name>`.

Not stored in SQLite: autofill settings and personal details (Android DataStore files `passcodes_autofill_settings`, `passcodes_autofill_information`), website icon files (`<documents>/favicons/<host>.png`), app settings (kv-store).

## 10. Native modules (Android / Kotlin)

Package root: `com.jeeldobariya.passcodes`.

### `modules/passcodes-native-database-service`
Single facade `PasscodesDatabase` (the only public class). Opens `<filesDir>/SQLite/master.db` per call; **never creates the file and never runs migrations** (the JS side owns the schema). Never throws (returns empty/`null`/`0`). Blocking I/O: call off the main thread. Functions: `getAllPasswords`, `filterPasswords(webDomain, packageName, fallbackToAll)`, `getPasswordById`, `countPasswords`, `savePassword` (upsert). `schema/`, `storage/`, `repository/` are `internal`. JS bridge is intentionally tiny (`getPasswordCount`) so credentials rarely cross the bridge.

### `modules/passcodes-autofill-service`
Android Autofill provider (API 26+). Fills saved logins and optional personal details, offers "Save password?". Has its own native settings and personal-details activities, biometric unlock activity, three presentation tiers (dropdown / inline keyboard chip / Presentations API). JS bridge: `isAutofillServiceEnabled`, `openAutofillSettings`, inline/biometric/fill-mode getters and setters, `getAutofillInformation` (read-only), `clearAutofillInformation`. Details are in the module's own `README.md` (read it before touching autofill).

Both modules ship `*.web.ts` stubs so the JS side can import them on web.

Native rules for agents:
- A Kotlin change cannot be verified in Expo Go or by `tsc`; say clearly in the PR what was and was not run on a device.
- Keep personal-detail classes redacted in logs (`<redacted>`); do not add `Log` calls that can print form content.
- Passwords are read/written exactly as stored (no crypto layer yet). The single place to add encryption for personal details is `AutofillInformationStore.encode/decode`.

## 11. How to... (recipes)

**Add or change a database column**
1. Edit `src/db/schema.ts`.
2. `npm run db:generate -- <descriptive_name>` (updates `src/db/drizzle/*` and `migrations.js`).
3. Update `PasswordsSchema.kt` (and `PasswordReader/Writer`) if native code needs the column.
4. Update `PasswordRepository` / `CategoryRepository` types, import/export (`formats/passcodesJson.ts`, `types.ts`) and `Business rules.md`.
5. Think about existing installs: the migration runs on app start for every user.

**Add a persisted setting**
Add a small store like `libs/home_layout.ts` (sync read, async write, `useSyncExternalStore`), use a `feat_<name>` key, define the default in one place, and add the row to `settings.tsx` and to the table in 6.5.

**Add a new import/export format**
Add a pure `formats/<name>.ts` (parse -> `ParseResult`, serialize), register it in `TransferFormat` and `TRANSFER_FORMAT_OPTIONS`, wire it in `importService.parseFile` and `exportService`, and keep the duplicate analysis untouched (it is format independent).

**Add a screen**
Create the route file under the right group in `src/app`, put reusable content in `features/<x>/ScreenContent.tsx`, wrap in `<Screen>`, register header options in the group's `_layout.tsx`, and use repositories through the context hooks.

**Add a sensitive action** (export, bulk removal, reveal of many secrets)
Call `confirmIdentity("<reason>")` and treat anything other than `isAuthenticated(outcome)` as a refusal; show `describeAuthFailure(outcome)` only when it is non-null (cancel is silent).

## 12. Known gaps, TODOs and inconsistencies (found in code)

Do not "fix" these silently; raise them or fix them deliberately in a dedicated PR.

1. **No encryption yet** for the DB, exported files, autofill personal details. `allowBackup` is forced off until it exists.
2. **Passcodes JSON version is `0`** (`PASSCODES_JSON_VERSION`), while the file header comment says "format version 1". Intended to become 1 (and version 0 invalid) once the format is stable.
3. **Delete category with "Delete passwords too"**: the confirmation says "permanently ... can't be undone", but `CategoryRepository.delete(id, "delete")` only **soft-deletes** the passwords (they appear in Recently deleted, category cleared). Text and behavior disagree.
4. **Legacy recovery screens can duplicate rows.** "Clear Previous Result (Unsafe)" on the GetBack screen resets the Room-migration marker and re-running inserts the old rows again; the Data Recovery screen (`test2.db`) has no marker at all and copies its rows every time it opens. Neither de-duplicates.
5. `categories` has a TODO for soft delete (`deleted_at`); category delete is permanent today.
6. `PasswordRepository.update` has a leftover `TODO` comment about `updated_at` handling; behavior is correct (`$onUpdateFn` + `pickEditablePasswordFields`).
7. `password-details.tsx`: when the id doesn't exist or is soft-deleted, the screen just returns (TODO: redirect / error state).
8. Duplicate generator routes (`(tabs)/password-generator.tsx` and `(settings)/password-generator.tsx`); the tab layout only declares two tabs, so check whether the first one shows up as an unintended third tab.
9. Web: `react-native-web`, a `web` script, `web` config and `.web.ts` module stubs exist, but `platforms` in `app.config.ts` is `["android", "ios"]`; web is not a supported target yet.
10. Autofill: no automated tests; not verified on a range of devices/keyboards/browsers; open performance, accessibility and security items are listed in the module README ("Future enhancements").
11. Planned (README checklist, not implemented): update checker, secure files, other secret info, non-secret info store, encryption/decryption, backup manager, custom database, translations, theming, key manager, multi-platform.
12. Memory/security: the import session holds plaintext passwords in memory until the review screen is left (it is cleared on `beforeRemove`).

## 13. Build variants and release

`app.config.ts` is the single place describing variants (`APP_VARIANT` = `development` | `preview` | `production`; anything else = production).

| Variant | Name suffix | Package suffix | ABIs (default) |
|---|---|---|---|
| development | ` Dev` | `.dev` | arm64-v8a, x86_64 |
| preview | ` Preview` | `.preview` | arm64-v8a, x86_64 |
| production | none | none | all four (override with `ANDROID_ABIS=a,b`) |

To release: bump `VERSION`, `BUILD_VERSION`, `BUILD_ANDROID_VERSIONCODE` (and iOS build number if needed) in `src/config.ts`, then build with the `deploy*` scripts. Release notes are generated by GitHub from PR labels per `.github/release.yml` (`highlights`, `feature`/`enhancement`, `bug`, `refactor`, `documentation`, `dependencies`).

## 14. Commits and pull requests

- Commit subject style used in the repo: `Type: Short description (#PR)` e.g. `Refactor: Improve Favicons & Introduce ApplicationIds (#244)`. Types seen/expected: `Feat`, `Fix`, `Refactor`, `Docs`, `Chore`.
- Fill in `.github/pull_request_template.md`: **Changes Made**, **Breaking Changes / Compatibility** (call out database changes, schema/format changes and anything affecting existing installs), **Notes**.
- Mention what you verified (lint, `tsc`, device run) and what you could not.
- Contributions are under the project's MIT license and its contribution terms (see README).

## 15. Quick checklist before you finish a change

- [ ] No secrets or user data in logs, toasts, errors, comments or test fixtures
- [ ] Soft-delete / no-overwrite behavior preserved
- [ ] Schema change => migration generated + Kotlin mirror updated + import/export considered
- [ ] Defaults for security flags unchanged (ON)
- [ ] `npm run lint` and `npx tsc --noEmit` pass
- [ ] `Business rules.md` and this file updated if a rule or structure changed
- [ ] `PRIVACY.md` updated if data handling, network use, permissions or storage changed
- [ ] PR description filled using the template
