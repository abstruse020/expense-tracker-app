# Expense Tracker — agent guide

## Overview

A small native Android expense tracker written in Kotlin with Jetpack Compose and Material 3.
Application id `com.example.expensetrackerv2` (do not change it: Android would treat it as a
different app and installs would not update). The visible app name is "Expense Tracker".
`minSdk` 26, `targetSdk`/`compileSdk` 36, single `app` module, no backend and no database.
Expenses live in a plain CSV file that the user picks (or creates) through Android's Storage
Access Framework; all other settings live in `SharedPreferences`.

## Versions

**1.0 (versionCode 1)** was the first working app: an Add Expense form, an Expenses list with
delete, and an Overview of spending per category and sub-category for a month (typed month/year).
Categories, sub-categories and payment modes were hardcoded in the source, and the expenses file
was chosen with New File / Existing buttons at the bottom of the Add Expense screen.

**1.1 (versionCode 2)** reorganized the UI into feature packages and added a Settings tab (expenses
file setup, which shows the current file name; editable categories, sub-categories and payment
modes), search on the Expenses list (tag-style field selector, description by default), month and
year dropdowns in Overview, a full light/dark Material 3 color scheme with no white flash at launch
in dark mode, rounder input corners, release signing, and the app rename (dropped "V2").

## Structure

```
app/src/main/java/com/example/expensetrackerv2/
├── MainActivity.kt          Entry point; NavigationSuiteScaffold with AppDestinations ADD / EXPENSES / OVERVIEW / SETTINGS
├── model/                   Expense, Category
├── data/
│   ├── CsvUriStore.kt       Persisted URI (and display name) of the user's CSV file
│   ├── CsvRepository.kt     Read / append / overwrite rows via ContentResolver
│   ├── ExpenseStorage.kt    saveExpense, readExpenses, deleteExpense, CSV escaping (contains legacy dead code)
│   ├── CategoryData.kt      Built-in default categories
│   ├── ExpenseOptionsStore.kt  User-editable categories / payment modes (SharedPreferences + JSON)
│   └── OptionValidator.kt   Name rules for categories / payment modes
└── ui/
    ├── theme/               Theme.kt (color schemes, shapes), Color.kt, Type.kt
    ├── components/          AppHeader, AppCard (AppCardContent), DropdownField
    ├── addexpense/          AddExpenseScreen
    ├── expenses/            ExpensesListScreen (date dividers + one card per expense; swipe left or long-press to delete), ExpenseSearch (pure search logic)
    ├── overview/            OverviewScreen (month/year dropdowns, spending bars)
    └── settings/            SettingsScreen (router), CategoriesPage, PaymentModesPage, OptionDialogs, CsvLaunchers
app/src/test/                JVM unit tests (OptionValidatorTest, ExpenseSearchTest)
app/src/main/res/values{,-night}/   Window theme + `window_background` color (prevents a white flash in dark mode)
```

## Data and conventions to respect

- CSV columns: `Date-Added, Category, Sub-Category, Description, Amount, PaymentMode, Date`.
  `Date-Added` (a timestamp) is the row id used for deletes.
- The reader uses `line.split(",")`, so names and descriptions must not contain commas, quotes or
  line breaks. `OptionValidator` enforces this for user-defined names. Do not loosen it without
  also fixing the CSV parser.
- Expenses store category, sub-category and payment mode as plain text, not ids. Deleting or
  renaming an option never changes existing rows; the Overview and list keep showing the old
  names. Renaming therefore only affects new expenses.
- Colors: use `MaterialTheme.colorScheme` roles in pairs (`primary`/`onPrimary`,
  `secondaryContainer`/`onSecondaryContainer`, ...). Do not hardcode colors. Dark mode defines the
  `surfaceContainer*` family explicitly; light mode relies on Material defaults.
- The `AppHeader` title is laid out below the gradient box, on the page background, so it uses
  `onBackground`, not `onPrimary`.
- Expense cards use `surface` (white) with a 1dp shadow in light mode and `surfaceContainerLow` in dark mode, chosen in `ExpenseItem`; the theme files are untouched.
- `ExpensesListScreen` and `OverviewScreen` load data once with `LaunchedEffect(Unit)`.

## Build, sign and release pipeline

JDK: no system `java`; use Android Studio's bundled one: `export JAVA_HOME=/opt/android-studio/jbr`.

```bash
./gradlew assembleDebug            # debug APK, signed with the local debug key
./gradlew testDebugUnitTest        # JVM unit tests
./gradlew assembleRelease          # signed release APK
```

Release output: `app/build/outputs/apk/release/app-release.apk`.

Signing is configured in `app/build.gradle.kts` (`signingConfigs.release`) and reads these Gradle
properties from `~/.gradle/gradle.properties` (outside the repo, `chmod 600`):

```properties
EXPENSE_TRACKER_STORE_FILE=/home/harsh/keystores/expense-tracker-release.jks
EXPENSE_TRACKER_STORE_PASSWORD=...
EXPENSE_TRACKER_KEY_ALIAS=expense-tracker
EXPENSE_TRACKER_KEY_PASSWORD=...
```

If `EXPENSE_TRACKER_STORE_FILE` is missing, the release build is produced unsigned (debug builds are
unaffected). Never commit the keystore or the passwords; `*.jks` and `*.keystore` are git-ignored.
The keystore must be backed up: losing it means future builds cannot be installed over existing ones.

Release steps:
1. Bump `versionCode` (must increase on every build handed to someone) and `versionName` in
   `app/build.gradle.kts`, and commit.
2. `./gradlew assembleRelease`.
3. Verify: `$JAVA_HOME/bin` on `PATH`, then
   `~/Android/Sdk/build-tools/<version>/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`
   should show the signer `CN=Harsh Pandey`, not the Android Debug certificate.
4. Share the APK (copy it to a name such as `expense-tracker-<versionName>.apk`). Recipients must
   allow "Install unknown apps" for the app they open it from.
