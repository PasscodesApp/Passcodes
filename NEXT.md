# NEXT.md

A guide for moving Passcodes forward: what is inconsistent today, which business
rules need attention, what to improve, and in which order. Everything here was
found by reading the code (see [`AGENT.md`](./AGENT.md) and
[`BUSINESS_RULES.md`](./BUSINESS_RULES.md)); nothing was changed or run on a
device. Treat it as a proposal for the maintainer, not a decision.

Priority legend: **P0** = do before calling it a stable release / security or
data-loss relevant, **P1** = next, **P2** = later / nice to have.
Effort: **S** (under a day), **M** (a few days), **L** (a week or more).

---

## 1. Inconsistencies found

### 1.1 Behavior vs. what the user is told

| # | What I found | Where | Why it matters |
|---|---|---|---|
| I-01 | *Delete passwords too* says "permanently ... can't be undone", but the repository only **soft-deletes** those passwords (they go to Recently deleted, category cleared). | `libs/category_actions.tsx`, `CategoryRepository.delete` | The UI promises something the code does not do; a user who wants a secret gone may think it is gone. |
| I-02 | Screenshot Prevention is described as "Doesn't allow screenshot", but it only blocks capture **while a password field is revealed**. Its text also says changes need a restart, yet the flag is read on every call. | `settings.tsx`, `features/screenshot-prevention/runtime.ts` | Over-promises protection; the restart note looks outdated. |
| I-03 | README features list shows *Import/Export* and *Encryption* unchecked, and says the stable release would be "around AUG, 2026". Import/Export exists now, and it is already past that date. App version string says `STABLE RC.3` while the README badge says BETA. | `README.md`, `src/config.ts` | Users cannot tell what is real or how stable it is. |
| I-04 | The "Autofill" and "Autofill Settings" rows in Settings both call `openAutofillSettings()`. | `settings.tsx` | Duplicate entry, likely leftover. |

### 1.2 Validation and data rules that disagree across layers

| # | What I found | Where |
|---|---|---|
| I-05 | Saving a password checks `!domain \|\| !username \|\| !password` **without trimming**, so a value of only spaces is accepted. Import rejects blank-after-trim domain/username. Edit does not re-check required fields at all. | `save-password.tsx`, `password-details.tsx`, `formats/*.ts` |
| I-06 | **Domain normalization differs**: JS import compares trim + lower-case; the app stores the domain exactly as typed; the Kotlin autofill save also strips scheme, `www.`, path, port and trailing dot. The same site can become two entries. | `utils/duplicates.ts`, `PasswordWriter.kt`, `DomainHelper.kt` |
| I-07 | Nothing enforces uniqueness of (domain, username). Kotlin picks "newest updated wins" when duplicates exist; JS import deliberately allows several versions. | `db/schema.ts`, `PasswordWriter.findExisting` |
| I-08 | The Passcodes JSON carries **category timestamps**, but import creates categories with `repository.create(name)` and **ignores** them. Password timestamps are kept. | `importService.resolveCategories`, `formats/passcodesJson.ts` |
| I-09 | Import duplicate analysis only looks at **active** passwords. Importing something that sits in Recently deleted creates a second copy. | `importService.prepareImport` (`getAll`) |
| I-10 | `PASSCODES_JSON_VERSION` is `0` while the file comment says "format version 1". The planned encrypted `.passcodes` format and a v1 spec exist outside the repo only. | `formats/passcodesJson.ts` |
| I-11 | The reserved category name check compares against `UNCATEGORIZED_LABEL` **and** the literal `"uncategorized"`; changing the label would leave the old name reserved. | `CategoryRepository.validateName` |

### 1.3 Security posture that is uneven

| # | What I found | Where |
|---|---|---|
| I-12 | **In-app lock defaults to ON, Autofill biometric lock defaults to OFF.** Passwords can be filled into other apps with no prompt unless the user finds the Autofill setting. | `runtime.ts`, `FillMode.kt`/autofill prefs |
| I-13 | Revealing a password needs no re-authentication (only export, bulk removal and raw DB export do). Copy exists only for generated passwords; saved passwords have no copy action and therefore no clipboard auto-clear to design yet. | `SecureTextField.tsx`, `ScreenContent.tsx` |
| I-14 | Autofill personal details and the password database are **unencrypted**; the single hook for details is `AutofillInformationStore.encode/decode`. | autofill module README |
| I-15 | Auto-lock time is a fixed 2 minutes; not configurable, and no "lock immediately" option. | `lockController.ts` |
| I-16 | The "Open" action navigates to the stored URL through `router.navigate(state.url as Href)` with a cast and no check of the scheme or an empty value. Worth reviewing how non-web or malformed values behave. | `password-details.tsx` |
| I-17 | Plaintext import data lives in an in-memory session until the review screen is left. Cleared on `beforeRemove`, but there is no clear if the process is backgrounded mid-review. | `importSession.ts` |

### 1.4 Housekeeping and structure

| # | What I found | Where |
|---|---|---|
| I-18 | Legacy recovery screens re-insert rows: GetBack can be re-run after resetting its marker; Data Recovery (`test2.db`) has no marker at all. Neither de-duplicates. | `get-back-passwords.android.tsx`, `data-recovery.tsx` |
| I-19 | `(tabs)/password-generator.tsx` is not declared in `Tabs.Screen`, so expo-router may show it as an extra tab, and a second copy lives under Settings. | `(tabs)/_layout.tsx` |
| I-20 | `package.json` version is `1.0.0` while the app is `v4.0.1`; no `typecheck` script; CI runs CodeQL only (no lint or `tsc`). | `package.json`, `.github/workflows` |
| I-21 | Web is half-supported: `react-native-web`, a `web` script, `web` config and `.web.ts` stubs exist, but `platforms` is `["android","ios"]`. | `app.config.ts`, `package.json` |
| I-22 | Some screens still use `SafeAreaView` directly instead of the central `Screen` wrapper, against the "one place" rule. | `settings.tsx`, `save-password.tsx`, others |
| I-23 | Categories have no soft delete (commented TODO); `password-details` has a TODO for the missing-id case; `PasswordRepository.update` has a stale TODO. | `schema.ts`, `password-details.tsx`, `PasswordRepository.ts` |
| I-24 | iOS bundle id still ends in `.earlybeta`; iOS has no autofill and the native modules are Android-only. | `app.config.ts` |

---

## 2. Business rules that need attention or change

| Rule | Today | Recommendation | Priority |
|---|---|---|---|
| BR-CAT-04 | "Delete passwords too" soft-deletes but is worded as permanent | Either make the wording honest ("moves to Recently deleted") or hard-delete after a clear confirmation. Prefer fixing the wording. | P0 / S |
| BR-PW-01 | Required fields not trimmed on save; edit does not enforce them | One shared validator (trim, required, max lengths) used by Add, Edit, import and autofill | P0 / S |
| BR-SEC-* (storage) | Everything plaintext | Master password + encryption (see 3.1) | P0 / L |
| BR-AF-05 | Autofill biometric lock off by default | Default ON when a screen lock exists (it already skips safely if not), or at least prompt on first enable | P0 / S |
| BR-RD-01 | Recently deleted never purges | Add a retention policy (for example 30 days, user-configurable, "never" allowed) and tell the user in the UI | P1 / S |
| BR-SEC-02 | Fixed 2-minute auto-lock | Setting: immediately / 1 / 2 / 5 / 15 min | P1 / S |
| BR-SEC-04 | Identity check on export, removal, raw DB export | Extend to revealing and copying saved passwords (optionally) | P1 / M |
| BR-IMP-02 | Duplicates only vs. active rows | Also compare against Recently deleted, or offer "restore instead" | P1 / M |
| BR-IMP-06 | Category timestamps dropped on import | Preserve them, or document as intended | P2 / S |
| BR-JSON-07 | Version constant `0` | Freeze the format, set to 1, reject 0 (see TODO in code); add the encrypted `.passcodes` container | P1 / M |
| BR-DB-05/06 | Legacy recovery can duplicate | Add a completion marker and de-duplicate by (domain, username, password), or retire them after a few releases | P2 / S |
| BR-SEC-06 | Screenshot blocking only on reveal | Fix wording; consider blocking the whole app (recents thumbnail and screenshots) as the stronger default | P1 / S |
| Domain rule (new) | Three different normalizations | Define one `normalizeDomain` and mirror it in Kotlin; apply at save time | P1 / M |

---

## 3. What to build, in priority order

### 3.1 P0: before a stable release

1. **Encryption at rest (L).** One master password unlocks the vault. Use a well-known, audited library and a modern password-based key derivation (Argon2id) with an authenticated cipher (XChaCha20-Poly1305 or AES-256-GCM), random salt and nonce per record or container, versioned header. Keep it a generic encrypt/decrypt module first (TypeScript, then a Kotlin twin) and prove both produce interoperable output with a couple of shared test vectors. Decide key handling early: derive once, keep the key in memory only while unlocked, optionally wrap with Android Keystore for biometric unlock. Plan the **migration of existing plaintext rows** and what happens if the user forgets the master password (there is no recovery; say so clearly).
2. **Autofill must work with encryption (M).** The Kotlin side reads the same database; it needs the key or an unlock handoff. Do this in the same milestone, not after, or autofill will break or force a weaker design. Encrypt the personal-details DataStore through the existing `encode/decode` hook.
3. **Fix the misleading text and validation (S):** I-01, I-02, I-05, I-12.
4. **Enable Android backup only after encryption (S)** and exclude the autofill DataStore file as the module README suggests.
5. **CI (S):** add a workflow running `npm ci`, `npm run lint` and `npx tsc --noEmit` on PRs; add a `typecheck` script.
6. **Truthful release metadata (S):** README status, feature checklist, version, privacy policy finalised (see `PRIVACY.md`).

### 3.2 P1: next

- **Encrypted backup/export** (`.passcodes`): password-protected container with a versioned header, same crypto module; keep plaintext JSON/CSV as an explicit, warned option.
- **Retention for Recently deleted**, configurable auto-lock, optional "lock on leaving the app".
- **Copy actions** for saved username/password with a **clipboard auto-clear** timeout and an identity check option.
- **One domain normalizer** shared by JS and Kotlin; decide whether (domain, username) should be unique.
- **Import polish:** compare with Recently deleted, preserve category timestamps, show a final summary of what will change before writing.
- **Password health:** weak/reused/old password report using the existing strength function (local only).
- **Accessibility pass** on the JS screens and the native autofill screens (module README lists items).
- **Performance on large vaults:** index on the normalized domain, `LIMIT` in SQL for "dumb" mode, short-lived caching of personal details (module README lists measured-not-profiled ideas; profile first).

### 3.3 P2: later

- Soft delete for categories; nested categories only if users ask.
- Update checker; secure notes/files; other secret info types (README roadmap).
- Translations and theme customization.
- Decide web: either finish it (async DB path already exists, add to `platforms`) or remove the scaffolding.
- Digital Asset Links verification before offering personal details to native apps; per-site consent for sensitive fields.
- Passkeys / TOTP storage (only after encryption is stable).

### 3.4 Process and quality (continuous)

- **Tests:** the project convention is *no automated tests*. The pure modules (`formats/*`, `utils/duplicates`, `authentication`, `lockController`, `application_ids`) are cheap to test and cover the highest-risk logic (data loss, security). Recommend the maintainer reconsider this for those files only; a small set of crypto test vectors is strongly advisable. This is a decision for the maintainer, so nothing was added.
- **Release checklist:** bump `src/config.ts` versions, update `PRIVACY.md` and `Business rules.md`, run lint and `tsc`, test on a real device with autofill on and off.
- **Security reporting:** keep `SECURITY.md` current; consider a published threat model (what Passcodes does and does not protect against).
- **Docs for contributors:** keep `AGENT.md`, `Business rules.md`, `NEXT.md` and `PRIVACY.md` in the same PR as the behavior they describe.

---

## 4. Suggested order of work (a simple roadmap)

1. **Quick wins (about a week, all S):** I-01, I-02, I-04, I-05, I-12, I-19, CI workflow, README/version cleanup, `typecheck` script.
2. **Crypto module** (TypeScript, then Kotlin) with shared test vectors; no app integration yet.
3. **Vault integration:** master password setup/unlock, encrypt/decrypt in `PasswordRepository`, migrate existing rows, adapt autofill, then turn on backup.
4. **Encrypted export/import** (`.passcodes`) using the same module; freeze Passcodes JSON v1.
5. **Behavior polish:** retention, configurable lock, copy with auto-clear, domain normalization, import improvements.
6. **Stable release:** finalised privacy policy, release notes, real-device test pass.
7. After release: P2 items based on user feedback.

## 5. How to use this file

- Pick items by ID (I-xx, BR-xx) in issues and PRs.
- When an item is done, delete it here and update the matching rule in `Business rules.md`.
- If you disagree with a priority, change it here; this file is meant to be edited.
