# Business rules

Summary of the rules Passcodes enforces today, written from the code. Each rule
has an ID so it can be referenced in issues, PRs and agent prompts, and a
**Source** pointing at where it is implemented. If you change behavior, update
the rule and its source in the same PR. For project structure and agent
guidance see [`AGENT.md`](./AGENT.md).

Conventions: "active" password = `deleted_at IS NULL`. "Uncategorized" =
`category_id IS NULL`. Timestamps are Unix epoch seconds (UTC).

> **Status warning (applies to everything):** passwords are stored and exported
> **unencrypted**. Encryption is planned, not implemented (see README).

---

## 1. Passwords

| ID | Rule | Source |
|---|---|---|
| BR-PW-01 | Saving a password requires **domain, username and password** to be non-empty. URL, notes, application IDs and category are optional. | `app/(tabs)/(password-manager)/save-password.tsx` |
| BR-PW-02 | A password created in the app gets `source = APPLICATION`. Imports use `IMPORTED`, autofill saves use `AUTOFILL`; migrated legacy rows have no source. | `PasswordRepository.create / importMany`, `PasswordsSchema.kt`, `db/schema.ts` |
| BR-PW-03 | The Add form preselects the category the user was viewing. When opened from inside a folder the category is **locked** to that folder (field hidden). A missing/invalid category param means Uncategorized. | `save-password.tsx`, `components/AddPasswordFab.tsx` |
| BR-PW-04 | Only these fields can be edited: domain, username, password, notes, url, application IDs, category. Anything else passed to `update` is dropped; `updated_at` is set by the database layer. Only **active** rows can be updated. | `repositories/passwordChanges.ts`, `PasswordRepository.update` |
| BR-PW-05 | Details screen is read-only until the user enters edit mode. While editing, pressing cancel or going back **discards** the changes, reloads the stored values and stays on the screen (the back action is intercepted). Saving writes the edit and returns to read-only mode. | `password-details.tsx` |
| BR-PW-06 | Deleting is a **soft delete** (`deleted_at = now`). Deleted passwords disappear from the list, counts, search, details lookup, export, duplicate analysis and autofill. | `PasswordRepository.delete / deleteMany / observeAll / getById` |
| BR-PW-07 | Search is a case-insensitive substring match (after trimming) on domain, username, url, notes, application IDs and category name (or "Uncategorized"). While searching, matching sections are force-expanded, empty categories are hidden and the folder grid is replaced by the match list. | `libs/category_groups.ts`, `(password-manager)/index.tsx` |
| BR-PW-08 | Multi-select: long-press starts selection, tap toggles, "select all" selects what is currently visible (respects the search filter). Bulk actions: move to a category (or Uncategorized) and delete (soft, with confirmation). Leaving the screen clears the selection; Android back leaves selection mode first. | `libs/selection_store.ts`, `components/SelectionBar.tsx` |
| BR-PW-09 | Bulk operations run in one transaction and batch ids in chunks of 500 (SQLite bound-variable limit). | `PasswordRepository` |

### Application IDs

| ID | Rule | Source |
|---|---|---|
| BR-APP-01 | An application ID is the Android package name / iOS bundle id a password belongs to, e.g. `com.example.app`. Valid format: starts with a letter, at least two dot-separated segments, characters `A-Z a-z 0-9 _ -`. | `libs/application_ids.ts` |
| BR-APP-02 | Input may be separated by commas, semicolons, spaces or new lines. Empty entries and repeats are dropped. Stored comma-joined in one column; `null` when there are none. | `libs/application_ids.ts` |
| BR-APP-03 | If any entry is invalid, **saving is blocked** with a message naming the first invalid entry (applies to Add and Edit). Imports from JSON silently drop invalid entries. | `save-password.tsx`, `password-details.tsx`, `formats/passcodesJson.ts` |

## 2. Recently deleted

| ID | Rule | Source |
|---|---|---|
| BR-RD-01 | Soft-deleted passwords are listed newest-deleted first. There is **no automatic purge**: they stay until restored or deleted forever. | `PasswordRepository.observeDeleted`, `features/recently-deleted/` |
| BR-RD-02 | **Restore** (one or all) clears `deleted_at`. If the password's category no longer exists it comes back as **Uncategorized**, never with a dangling category. | `PasswordRepository.restoreMany` |
| BR-RD-03 | **Delete forever** (one or all) asks for confirmation and only removes rows that are already soft-deleted, so an active password can never be destroyed this way. | `PasswordRepository.permanentlyDeleteMany / permanentlyDeleteAllDeleted` |

## 3. Categories (folders)

| ID | Rule | Source |
|---|---|---|
| BR-CAT-01 | Categories are **flat** (no nesting). "Uncategorized" is virtual (`category_id IS NULL`), is not a row and cannot be renamed or deleted. | `db/schema.ts` |
| BR-CAT-02 | Name is trimmed with inner whitespace collapsed, must be non-empty, at most **40** characters, must not be the reserved name "Uncategorized" (any case), and must be unique case-insensitively (renaming to its own current name is allowed). | `CategoryRepository.validateName / assertNameAvailable` |
| BR-CAT-03 | Categories are **created from the home screen** (FAB) and renamed/deleted from the category actions menu. The Add/Edit password form can only **select** a category. | `components/HomeFab.tsx`, `libs/category_actions.tsx`, `components/CategoryField.tsx`, `CategoryPickerSheet.tsx` |
| BR-CAT-04 | Deleting a category runs in **one transaction**. Default and recommended choice: *Move to Uncategorized* (passwords stay, `category_id = NULL`). Choosing *Delete passwords too* needs a **second confirmation**. An empty category only needs one confirmation. The category row itself is removed permanently. | `CategoryRepository.delete`, `libs/category_actions.tsx` |
| BR-CAT-05 | A password whose category no longer exists is treated as Uncategorized in the UI so it can never silently disappear. Database FK is `ON DELETE SET NULL`. | `libs/category_groups.ts`, `db/schema.ts` |
| BR-CAT-06 | Two home layouts, chosen in Settings (default **grouped**): *grouped list* = one collapsible section per category (empty categories shown when not searching; Uncategorized section only when it has passwords); *folders* = grid of folders (the Uncategorized folder is **always present**, so there is always a place to add a password). Both are derived from one hook so they never disagree. | `libs/home_layout.ts`, `libs/category_groups.ts` |
| BR-CAT-07 | In the folders layout, new passwords are added from inside a folder (category locked, BR-PW-03); the home FAB only creates folders. In the grouped layout the FAB offers "New category" and "Add password". | `components/HomeFab.tsx` |

> Known inconsistency: for *Delete passwords too*, the dialog says "permanently ... can't be undone", but the repository **soft-deletes** those passwords (they land in Recently deleted with their category cleared). See AGENT.md section 12.

## 4. Security and privacy

| ID | Rule | Source |
|---|---|---|
| BR-SEC-01 | **App Lock** is ON by default. If enabled, the app starts locked and shows the unlock prompt immediately. | `features/biometrics/runtime.ts`, `useAppLock.ts` |
| BR-SEC-02 | After the app is backgrounded for **more than 2 minutes (120,000 ms)** it locks again when it returns. Shorter trips away (including the system biometric prompt itself) never re-lock. Only an enabled lock re-locks. | `features/biometrics/lockController.ts` |
| BR-SEC-03 | Authentication outcomes: `success`, `not-required`, `cancelled`, `lockout`, `failed`, `error`. Only `success` and `not-required` allow the action. Anything unexpected is a **refusal (fail-closed)**. The single deliberate exception is a device with **no screen lock at all** (`not-required`): refusing would lock the user out of their own data. Biometrics may fall back to the device PIN/pattern/password. Concurrent requests share one prompt. | `features/biometrics/authentication.ts` |
| BR-SEC-04 | `confirmIdentity()` is required **even if App Lock is switched off** for: exporting passwords, removing existing passwords during import review, and exporting the raw database from the recovery screen. A cancelled prompt is silent; lockout/failure/error show a message. | `ExportScreenContent.tsx`, `ImportReviewScreenContent.tsx`, `DatabaseRecoveryScreen.tsx` |
| BR-SEC-05 | A missing, unreadable or unrecognised stored security switch falls back to its default (**ON**). `set()` returns the value actually stored so the UI never shows a position that was not saved. | `features/biometrics/flag.ts` |
| BR-SEC-06 | **Screenshot prevention** is ON by default. While it is on, screen capture is blocked whenever a password field is **revealed** (eye toggle) and released when hidden or when the screen unmounts. Turning the setting off disables blocking entirely (the setting text says a restart is needed). | `features/screenshot-prevention/`, `components/SecureTextField.tsx` |
| BR-SEC-07 | Password fields are masked by default and reveal only on explicit toggle. | `components/SecureTextField.tsx` |
| BR-SEC-08 | Android backup is disabled and the app is flagged as holding fragile user data. Backup may be enabled only once encryption exists. | `plugins/withProtectUserData`, `app.config.ts` |
| BR-SEC-09 | User-visible messages and logs about import/export/auth contain **counts and fixed text only**, never values from the user's data. | `features/import-export/types.ts` (`FAILURE_REASON_TEXT`), services |
| BR-SEC-10 | Exported files are **not encrypted**; the export screen says so and tells the user to keep the file safe. | `ExportScreenContent.tsx`, `TRANSFER_FORMAT_OPTIONS` |
| BR-SEC-11 | Plaintext import data (the parsed file and plan) lives **in memory only** and is discarded when the review screen is left. The picked file is copied to cache, read, and the copy deleted. | `features/import-export/importSession.ts`, `ImportReviewScreenContent.tsx`, `files.ts` |
| BR-SEC-12 | Generated passwords use a cryptographically secure random source. | `features/password-generator/utils.ts` |

## 5. Import

Formats: **Google CSV** and **Passcodes JSON**. Workflow: pick -> read -> parse -> compare with the database (`prepareImport`), then the user reviews conflicts, then `commitImport` writes. Nothing is written before the user confirms.

| ID | Rule | Source |
|---|---|---|
| BR-IMP-01 | Cancelling the picker is not an error. A readable file with no password records is "empty". If records exist but none are valid, the import fails with "none valid, nothing imported". File-level problems abort the whole import. | `services/importService.ts` |
| BR-IMP-02 | **Exact duplicate** = same normalized domain (trimmed, lower-cased), same username (exact) and same password (exact) as an **active** existing password or an earlier record in the same file. Exact duplicates are **skipped** and counted. Usernames/passwords are compared exactly because discarding a record is the destructive direction. | `utils/duplicates.ts` |
| BR-IMP-03 | **Conflict** = same normalized domain + username but a different password (against existing passwords or between records in the file). Conflicts are never resolved silently; the user reviews each group. Everything else is imported without asking. | `utils/duplicates.ts` |
| BR-IMP-04 | Review defaults: **keep both versions of every conflict, remove nothing**. The user can switch off "import" per incoming record and choose existing passwords to remove. | `defaultDecisions`, `ImportReviewScreenContent.tsx` |
| BR-IMP-05 | Existing passwords chosen for removal are removed only if at least one incoming record of that group was **actually written**, so a failed import never costs the user the password it was meant to replace. Removal is a soft delete (goes to Recently deleted) and needs identity confirmation (BR-SEC-04). | `removableExistingIds`, `commitImport` |
| BR-IMP-06 | Categories are resolved **before** passwords. A file category whose normalized name (trim, collapsed whitespace, case-insensitive) matches an existing category **reuses** it; others are created. If a category cannot be created (invalid/reserved name) its passwords are imported as Uncategorized and a warning is shown. All categories in the file are created even if empty. | `resolveCategories` |
| BR-IMP-07 | Imported rows get `source = IMPORTED`, keep file timestamps when present (missing/invalid -> now), are written in transactions of **200** rows, and fall back to row-by-row insert if a chunk fails. If rows were attempted and none could be written the import reports failure. | `PasswordRepository.importMany` |
| BR-IMP-08 | The result reports: detected, imported, skipped (exact duplicates + conflicts the user chose not to import), failed (invalid records + save failures), categories created, existing passwords removed, a per-reason failure summary and warnings. | `ImportOutcome` in `types.ts` |

### Google CSV (`name,url,username,password,note`)

| ID | Rule | Source |
|---|---|---|
| BR-CSV-01 | Header names are trimmed and lower-cased. Required columns: `name`, `username`, `password`. `url` and `note` are optional. Duplicate column names, an empty file, or a quote that is never closed make the **whole file** invalid. | `formats/googleCsv.ts` |
| BR-CSV-02 | A row with the wrong number of columns or malformed quoting fails **only that row**. Blank/separator-only lines are ignored. All fields stay strings (no number conversion, a leading BOM is removed). | `formats/googleCsv.ts` |
| BR-CSV-03 | Mapping: `name` -> domain (if empty, the host of `url` without scheme, user-info and `www.`), `url` -> url, `username`, `password`, `note` -> notes (empty -> null). Domain and username must be non-blank after trimming and password non-empty, otherwise the row is rejected (`missing-domain` / `missing-username` / `missing-password`). | `formats/googleCsv.ts` |
| BR-CSV-04 | The placeholder URL `https://local.<domain>` means "no URL": it is written on export for entries without a URL and turned back into `null` on import. | `placeholderUrl()` |
| BR-CSV-05 | CSV cannot carry categories, application IDs or timestamps: imported rows are Uncategorized, have no application IDs and get the import time. Export does not escape formula characters (that would alter passwords starting with `=`, `+`, `-`, `@`). Line endings are CRLF. | `formats/googleCsv.ts` |

### Passcodes JSON backup

Shape: `{ format: "passcodes", version, "exported-at", counts: {passwords, categories}, data: { passwords: [...], categories: [...] } }`.

| ID | Rule | Source |
|---|---|---|
| BR-JSON-01 | Every item in `data` has an `_id` that is **unique across the whole file**: passwords are numbered `1..n`, categories continue at `n+1`. `_id`s are file-local, never database ids. A password references its category through `category-id` (the category's `_id`, or `null`). | `formats/passcodesJson.ts` |
| BR-JSON-02 | Import is **strict about structure** and **lenient about cosmetics**. File-level errors: not valid JSON, `format` is not `"passcodes"`, version missing/invalid, **version newer than the app understands** (asks the user to update), missing `data`/`counts`/lists, or `counts` not equal to the real list lengths (file may be incomplete or edited). | `parsePasscodesJson` |
| BR-JSON-03 | Per-password rules: `domain`, `username`, `password` must be strings; domain and username non-blank, password non-empty; `url`, `notes`, `application-ids` are optional strings (blank -> null); `category-id` is a positive integer or null; `_id` must be a positive integer not used earlier. Violations reject only that record. | `parsePasscodesJson` |
| BR-JSON-04 | Per-category rules: non-blank string `name` and a unique positive `_id`. | `parsePasscodesJson` |
| BR-JSON-05 | Timestamps must be integers in epoch **seconds** within `0 .. 253402300799` (to 9999-12-31 UTC). Invalid ones become null (imported with the import time) and add a warning. Milliseconds, strings and fractions count as invalid. | `readTimestamp`, `libs/timestamp_unixepoch_utils.ts` |
| BR-JSON-06 | A password pointing at a category that is not in the file is **still imported** (as Uncategorized) with a warning. Unknown sections under `data` are ignored with a warning. Invalid application IDs are dropped. | `parsePasscodesJson` |
| BR-JSON-07 | The format version constant is currently **0** (pre-stable). It is meant to become 1, with 0 then rejected. Additive changes keep the version; breaking changes increase it. | `PASSCODES_JSON_VERSION` |

## 6. Export

| ID | Rule | Source |
|---|---|---|
| BR-EXP-01 | Export needs identity confirmation first (BR-SEC-04). With no active passwords, export reports "nothing to export". A cancelled Save dialog is not an error. | `ExportScreenContent.tsx`, `services/exportService.ts` |
| BR-EXP-02 | Exports contain **active passwords only** (soft-deleted are excluded). Passcodes JSON additionally contains **all categories**. Google CSV never contains categories or timestamps. | `exportService.ts` |
| BR-EXP-03 | File names: `passcodes-export-YYYY-MM-DD.csv` and `passcodes-backup-YYYY-MM-DD.json` (UTC date). The count shown on screen is re-read every time the screen gains focus. | `exportService.ts`, `ExportScreenContent.tsx` |

## 7. Password generator

| ID | Rule | Source |
|---|---|---|
| BR-GEN-01 | Defaults: length **16**, uppercase and lowercase on, numbers and symbols off. Length range **4 to 64**. | `features/password-generator/utils.ts` |
| BR-GEN-02 | At least **one character type must stay enabled** (the last enabled one cannot be switched off). | `ScreenContent.tsx` |
| BR-GEN-03 | Changing an option regenerates immediately, so the shown password always matches the options. A strength label/bar is computed from the shown password. The password can be copied to the clipboard. | `ScreenContent.tsx` |

## 8. Website icons (favicons)

| ID | Rule | Source |
|---|---|---|
| BR-FAV-01 | Provider is a setting: Google (default), DuckDuckGo, Favicon.im, or **Off**. Only the **host name** is sent, only to the chosen provider, and nothing is requested when the provider is Off (already cached icons still show). An older "icons off" flag is honored as Off. | `features/favicons/providers.ts`, `setting.ts`, `cache.ts` |
| BR-FAV-02 | A host is looked up only for real web hosts: needs a dot, valid characters, `http(s)` or no scheme; **never** for non-web schemes (e.g. `androidapp://`), `.local` / `.internal` / `.lan`, or private/loopback/link-local IP ranges (10.x, 127.x, 192.168.x, 172.16-31.x, 169.254.x). Entries without a web address cause no request. | `getFaviconHost` |
| BR-FAV-03 | Cache: a found icon is reused for **30 days** then refreshed once; a failed lookup is retried after **1 day** (or immediately if the user picks another provider); at most **4** downloads at once and one per host; an empty download is a failure; a failed refresh keeps the old icon. | `features/favicons/cache.ts` |

## 9. Autofill (Android)

Settings are shared by the native service, the native settings screen and the React Native bridge (one DataStore file `passcodes_autofill_settings`).

| ID | Rule | Source |
|---|---|---|
| BR-AF-01 | Requires Android 8.0 (API 26)+. Passcodes must be chosen as the system Autofill service by the user; Settings shows Enabled/Disabled (re-checked whenever the app returns to the foreground). | `PasscodesAutofillService.kt`, `settings.tsx` |
| BR-AF-02 | **Fill mode** (user setting): `smart` = only credentials matching the site/app, nothing otherwise; `auto` (**default**) = matching first, otherwise everything; `dumb` = always everything. Unknown stored values fall back to `auto`. | `FillMode.kt` |
| BR-AF-03 | Matching: the web domain is normalized (scheme, `www.`, path, port, trailing dot removed, lower-cased) and matches the stored domain **exactly or as a subdomain** (stored `google.com` matches `accounts.google.com`, not the reverse). The app package matches **one exact segment** of `application_ids` (case-insensitive) and is never compared to `domain`. Soft-deleted rows are always excluded. | `passcodes-native-database-service` (`PasscodesDatabase.filterPasswords`) |
| BR-AF-04 | At most **20** saved-login suggestions (newest-updated first) and **6** personal-detail suggestions per screen. | `PasscodesAutofillService.kt` (`MAX_DATASETS`, `MAX_IDENTITY_DATASETS`) |
| BR-AF-05 | **Biometric lock for autofill** is a separate, **opt-in (default off)** switch from the in-app lock. It is active only when enabled **and** the device can authenticate; otherwise it is skipped so the user is never locked out. When active, suggestions are "Tap to unlock" rows, passwords are not read until after a successful prompt (then read by row id). | `resolveBiometricGateActive`, `AutofillUnlockActivity` |
| BR-AF-06 | **Inline keyboard suggestions** are on by default (API 30+). A locked inline chip shows the site name and "Tap to unlock", never the username. | module README, `AutofillPresentationFactory` |
| BR-AF-07 | **Save**: after a login form is submitted the user is offered "Save password?". A blank password is not saved. The save runs to completion even if the service is torn down (a typed password must not be lost). | `onSaveRequest` |
| BR-AF-08 | Save upserts by **(normalized domain, username)** among non-deleted rows (newest-updated wins if duplicates exist). *Existing*: updates password, notes (autofill marker replaced, not stacked), merged application IDs and `updated_at`; an existing non-blank URL is never overwritten. *New*: `source = AUTOFILL`, note "saved using autofill; needs user review.", URL = form URL or `https://local.<domain>`. | `PasswordWriter.kt` |
| BR-AF-09 | Web form: `domain` = web domain, no application ID added. Native app form: `domain` = the app's label (fallback package name) and the package name is appended to `application_ids` (de-duplicated). A typed new-password field is preferred over the password field. | `handleSaveRequest` |

### Personal details autofill

| ID | Rule | Source |
|---|---|---|
| BR-PD-01 | Fills first/last/full name, up to 5 emails, up to 5 phone numbers (full / country code / national), birth date and address on forms that are **not sign-in screens**. Sign-in screens get saved logins only, unless the user triggers a manual autofill request. | module README, `identityFieldsToOffer` |
| BR-PD-02 | **Requires a secure screen lock** (PIN/pattern/password/biometric). Without one, no personal detail is ever filled. | `DeviceSecurity.kt`, `PasscodesAutofillService.kt` |
| BR-PD-03 | Honors the same biometric-lock switch as saved logins; unlock intents carry field ids, never the data. | `IdentityUnlockExtras`, `buildLockedIdentityDataset` |
| BR-PD-04 | **Address and birth date** are never offered on `http://` pages or on pages mixing several web origins (e.g. third-party iframe); only the contact bundle may be offered there. | `identityFieldsToOffer` |
| BR-PD-05 | Values that do not fit a field are **skipped, never truncated** (maxlength, numeric-only inputs, drop-downs without the exact option, unknown date formats). Detection is heuristic: missing a field is preferred over mis-filling it. | module README |
| BR-PD-06 | Details are entered only on the **native** personal-details screen, stored in their own DataStore (`passcodes_autofill_information`), **local only**: not in the SQLite database, not backed up, not imported or exported. They are **not encrypted yet**. Data classes print as `<redacted>`. | module README, `AutofillInformationStore` |

## 10. Database lifecycle, recovery and legacy migration

| ID | Rule | Source |
|---|---|---|
| BR-DB-01 | The app renders only after the database is opened and **all migrations succeeded**. On failure a plain-language **recovery screen** is shown instead; "Try again" repeats the whole start-up. | `db/provider.tsx` |
| BR-DB-02 | Recovery never deletes, resets or modifies the user's data. Connections are intentionally never closed (closing the last WAL connection would merge and delete the `-wal` file). | `db/provider.tsx`, `DatabaseRecoveryScreen.tsx` |
| BR-DB-03 | Raw database export: shows a warning first, asks for identity confirmation (BR-SEC-04), copies the `.db` and its `-wal`/`-journal` side files to a private temp folder (retrying up to 3 times if they change while copying), merges the WAL into the **copy**, lets the user choose a destination, then deletes the temp folder. If the copy cannot be opened, the raw files are saved as they are. | `db/exportDatabase.ts` |
| BR-DB-04 | The JavaScript side owns the schema and migrations. The native database module **never creates the file and never migrates**; without the file, reads are empty and writes return `null`. | `modules/passcodes-native-database-service` |
| BR-DB-05 | **Room -> Drizzle migration** (Android, from the older native app, DB versions 1 and 2): copies the old database into a temporary workspace first, aborts on any problem, normalizes timestamps (seconds, milliseconds or SQLite datetime strings -> epoch seconds), gives v1 rows the placeholder URL `https://local.<domain>`, inserts in one transaction and **verifies** that inserted count equals source count; temporary files are cleaned up. State is tracked with `room_expo_migration_complete_v1` (`running` / `success` / `failed`); a completed migration is not repeated unless the user resets the marker. | `libs/room_drizzle_migration.ts`, `get-back-passwords.android.tsx` |
| BR-DB-06 | The Data Recovery screen copies every row of the old mistaken database `test2.db` into `master.db` in one transaction (timestamps normalized). If `test2.db` has no `passwords` table it is deleted instead. There is **no "already done" marker and no de-duplication**: opening the screen again copies the rows again. `Config.OLD_MISTAKEN_DATABASE_NAME` must never be edited. | `libs/data_recovery_test_db_mess.ts`, `data-recovery.tsx`, `config.ts` |

## 11. Cross-cutting consistency notes

- **Domain comparison differs by layer.** JS import/duplicate analysis normalizes a domain by trimming and lower-casing only. The Kotlin autofill save normalizes more (scheme, `www.`, path, port, trailing dot). The same site could therefore be treated as two entries across layers.
- **Delete semantics differ by place.** Password delete, import-removal and category "delete passwords too" are all **soft** deletes; only "Delete forever" in Recently deleted and category-row removal are permanent.
- **Timestamps** are epoch seconds in the DB, the JSON backup and the native layer; always convert legacy/ms/ISO values with `normalizeTimestamp`.
- **Single sources of truth:** schema = `src/db/schema.ts` (+ Kotlin mirror `PasswordsSchema.kt`); safe area = `src/libs/safe_area.ts`; version = `src/config.ts`; "Uncategorized" label = `UNCATEGORIZED_LABEL` in `CategoryRepository.ts`.

## 12. Not implemented yet (do not assume it exists)

Encryption / decryption of the database, backups and exports; encrypted backup files; update checker; backup manager; secure files; other secret/non-secret info stores; key manager; translations; theme customization; supported web target; soft delete for categories; auto-purge of Recently deleted.
