# Passcodes native modules

Two sibling Expo modules. Put both folders side by side wherever your
other local Expo modules live (e.g. `modules/`).

```
passcodes-native-database-service/   all SQLite access (shared core)
  database/
    PasscodesDatabase.kt             public facade — the only class other modules use
    PasscodesNativeDatabaseModule.kt JS bridge (getPasswordCount)
    model/       PasswordEntry
    schema/      PasswordsSchema     table/column names, source + note markers
    storage/     DatabaseConnection  DB file location, open read/write
    repository/  PasswordReader, PasswordWriter
    util/        DomainHelper, AppIdentifierHelper

passcodes-autofill-service/          Android Autofill; depends on the database module
  PasscodesAutofillServiceModule.kt  JS bridge (settings)
  service/       PasscodesAutofillService
  parser/        AutofillRequestParser, AutofillValues
  presentation/  AutofillPresentationFactory (dropdown + inline), AutofillSaveUiFactory ("Save password?" card)
  settings/      AutofillPreferences, FillMode, AutofillSettingsActivity
  unlock/        AutofillUnlockActivity
```

A autofill & also importer/exporter module should depend on the database module
and call `PasscodesDatabase` — nothing else needs to know
about SQLite.
