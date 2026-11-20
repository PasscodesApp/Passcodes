# Privacy Policy

**App:** Passcodes (Android and iOS builds from this repository)
**Version this policy describes:** v4.0.1 (STABLE RC.3)
**Effective date:** 2026-11-19

> **DRAFT.** This privacy policy is a draft. It describes how Passcodes works today
> as best we understand it, but it has not been through a formal review and it is
> not legal advice. We plan to make it more complete and more effective in the near
> future, and making it effective as soon as possible is a priority for the project.
> If something here is unclear or looks wrong, please tell us (see Contact).

> **Please read first.** Passcodes is a beta, open-source project. Your data is
> **not encrypted yet** (neither on your device nor in exported files).
> Encryption is planned but not implemented. Until a stable release with
> encryption ships, treat Passcodes as a tool for trying things out, not as the
> only place your important passwords live.

## 1. The short version

- Passcodes is **local-first**. Your passwords stay **on your device**. There is no account, no sign-up and no developer server that stores your data.
- The developers **do not collect, receive, sell or share** your passwords or personal details.
- There is **no analytics, advertising, tracking or crash-reporting** code in the app.
- The **only** automatic network request the app makes is downloading **website icons**, and it sends only the **website's host name** (for example `example.com`) to the icon provider you choose. You can turn this off.
- Anything that leaves your device otherwise does so because **you** exported a file, copied a password, used Autofill, or opened a link.

## 2. What information the app handles

All of this is stored on your device only.

| Information | Why | Where it is stored |
|---|---|---|
| Saved logins: domain/site name, username, **password**, notes, URL, application IDs (Android package names / iOS bundle IDs), category, created/updated/deleted timestamps | To be your password manager | App-private SQLite database `master.db` |
| Categories (folders) you create | To organise passwords | Same database |
| App settings: App Lock on/off, screenshot protection on/off, home layout, website-icon provider | To remember your choices | App-private key-value store |
| Website icon images and a small record of when each was fetched | To show icons without downloading them again | App-private files (`favicons` folder) and key-value store |
| Autofill settings: fill mode, biometric lock for Autofill, inline suggestions | To control Autofill | App-private Android DataStore |
| **Autofill personal details** (optional): name, up to 5 emails, up to 5 phone numbers, birth date, address | To fill forms for you when you ask for it | App-private Android DataStore, separate from the password database |
| Old data from earlier Passcodes versions (only if you run the migration/recovery tools in Settings) | To bring your old passwords back | Copied into `master.db` |

Passcodes does **not** read your contacts, location, camera, microphone, photos or SMS, and contains no feature that does.

**Biometrics.** App Lock uses your device's own biometric or screen-lock prompt. The app only receives a yes/no result from the operating system. Fingerprints, face data and your device PIN/pattern/password are never available to Passcodes.

## 3. What can leave your device

### 3.1 Website icons (automatic, can be turned off)

When a saved entry has a real web address, the app may download that site's icon so it can show it next to the entry.

- **What is sent:** only the site's **host name**, to the provider you pick in *Settings > Website icons*. Like any web request, the provider also sees your **IP address** and ordinary request metadata, and can therefore tell which sites you have saved.
- **Providers:** Google (default), DuckDuckGo, Favicon.im, or **Off**. With *Off*, nothing is downloaded (icons you already have may still be shown).
- **Never requested for:** entries without a web address, app-only entries (non-web schemes), names without a dot, `.local` / `.internal` / `.lan` hosts, or private, loopback and link-local IP addresses.
- **Your username, password, notes and full URL path are never sent.**
- Each provider has its own privacy policy that applies to what it receives: [Google](https://policies.google.com/privacy), [DuckDuckGo](https://duckduckgo.com/privacy), and Favicon.im (see its website).
- Icons are reused for 30 days; a failed lookup is retried after 1 day.

### 3.2 Files you export

Export in *Settings* creates a **Google-style CSV** or a **Passcodes JSON backup** containing your passwords in **plain text**, and the recovery screen can export your raw database file. You choose where the file is saved with the system Save dialog. Passcodes asks you to confirm your identity first, but **once the file exists, anyone who can open it can read your passwords**. Keep exported files somewhere safe and delete them when you no longer need them. The developers never receive these files unless you choose to send one (for example when asking for support).

### 3.3 Autofill (Android)

If you enable Passcodes as your Android Autofill service, Android gives Passcodes the structure of the screen you are filling so it can find login and form fields. Passcodes then gives matching saved logins, or personal details if you set them up, to the app or browser you are using, through the Android Autofill framework. This stays on your device. You control it in the Autofill settings (fill mode, biometric lock, inline suggestions). Personal details are filled only if the device has a secure screen lock, and values that do not fit a field are skipped.

When you submit a login form, Android may offer to save it; if you accept, Passcodes stores it in your local database.

### 3.4 Clipboard

The password generator can copy a generated password to your clipboard when you tap *Copy*. Other apps on your device may be able to read the clipboard depending on your Android/iOS version and settings.

### 3.5 Links you open

Tapping *Open* on a saved entry opens its URL, and the About card links to email, GitHub, Telegram and Discord. These open in your browser or another app and are governed by that app and website's own policies. The recovery screen also offers community links.

## 4. Security measures (and their limits)

What the app does today:

- **App Lock** (on by default) with biometrics or device credential, re-locking after 2 minutes in the background. If your device has no screen lock at all, there is nothing to ask, so the app opens.
- **Identity confirmation** before exporting, before removing passwords during import review, and before exporting the raw database.
- **Screenshot protection** (on by default) while a password is revealed.
- **Android backup is disabled** and the app is marked as holding fragile user data, so your database is not copied to cloud backups.
- Passwords are masked until you reveal them, and the app avoids putting your data in logs and error messages.
- The password generator uses a cryptographically secure random source.

What it does **not** do yet:

- **No encryption at rest.** Your database, the Autofill personal details and exported files are stored unencrypted. Someone with access to an unlocked device, a rooted device or the app's private storage could read them.
- Your security therefore also depends on your **device lock, OS updates and physical control** of the phone.

If you find a security problem, please report it **privately** to the maintainer addresses in [`SECURITY.md`](./SECURITY.md) and not in a public issue.

## 5. How long data is kept, and deleting it

- Data stays on your device until **you** remove it.
- Deleting a password moves it to **Recently deleted**. It is **not removed automatically**; it stays until you choose *Delete forever* (one or all).
- Deleting a category does not by itself erase passwords unless you choose that option.
- **Uninstalling the app** or clearing its storage removes the database, settings, Autofill details and cached icons held in the app's private storage. It does **not** remove files you exported or copied elsewhere.
- Because the developers hold no copy of your data, they cannot retrieve or restore it for you, and there is nothing on their side to delete.

## 6. Your choices

You can, at any time and inside the app: turn App Lock and screenshot protection on or off, change or disable website icons, change Autofill behaviour or disable the Autofill service in Android settings, clear Autofill personal details, delete individual passwords or empty Recently deleted, and export or import your data.

## 7. Children

Passcodes is a general-purpose tool and is not directed at children. It does not ask for age or collect personal data from anyone.

## 8. Where you get the app

Official builds are published through the project's [GitHub Releases](https://github.com/PasscodesApp/Passcodes/releases/latest) and the Komi Store listing referenced in the README. Those platforms have their own privacy practices (for example about downloads), which this policy does not cover.

## 9. Open source and verification

Passcodes is licensed under the MIT license and its source code is public, so you can check the statements above. In particular, the only network download in the app is the icon fetch in `src/features/favicons/`, and there is no analytics or tracking dependency in `package.json`.

## 10. Changes to this policy

Changes are made through this repository, so every version is visible in its history. The effective date above changes when the policy changes. If a future version adds network features, accounts, sync, analytics, or changes how data is stored (for example encryption or backups), this policy will be updated **before** that release.

## 11. Contact

Questions about this policy: **jeeldobariya38@gmail.com**, or the project's [GitHub repository](https://github.com/PasscodesApp/Passcodes) and community channels listed in the README.
Security vulnerabilities: see [`SECURITY.md`](./SECURITY.md).

---

### Keeping this policy accurate (for maintainers and AI agents)

Update this file in the same PR whenever you:

- add **any** network request, SDK, analytics, crash reporting, update checker, sync or cloud feature (current sole exception: `src/features/favicons/`);
- add or change an icon provider (`src/features/favicons/providers.ts`);
- add encryption, enable Android backup (`allowBackup` in `app.config.ts`), or change where data is stored;
- add a new permission, a new category of data (for example new Autofill detail fields), or a new export/import format;
- change App Lock timing, screenshot protection defaults, or what `confirmIdentity` guards;
- change deletion behaviour (for example auto-purge of Recently deleted).

See [`AGENT.md`](./AGENT.md) and [`Business rules.md`](./Business%20rules.md) for the rules this policy summarises.
