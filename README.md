This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Android API configuration

The Android app uses the production API directly. Set its origin (including scheme and host) with the `loanApiBaseUrl` Gradle property or `LOAN_API_BASE_URL` environment variable. The value must end at the API origin; endpoint paths such as `/api/loans` are appended by the app.

```text
./gradlew :androidApp:assembleDebug -PloanApiBaseUrl=https://your-api-host/
```

Release packaging rejects the placeholder API host and any non-HTTPS URL. `API (3).md` documents API routes but does not specify the deployed host, so configure the real host before making a release build.

For local mock-backend testing, install the Python dependencies once with `python -m pip install -r requirements.txt` from `androidApp/src/main`, then run `python main.py` in that directory. The server listens on port `8000`. Then install the debug app with:

```text
.\gradlew.bat :androidApp:installDebug -PloanApiBaseUrl=http://10.0.2.2:8000/
```

`10.0.2.2` reaches the development computer from the Android emulator. On a physical phone, use the computer's LAN IP instead, and make sure the phone can reach port `8000`. The debug manifest alone permits HTTP. The release manifest still blocks cleartext, and release tasks reject HTTP hosts. The mock manager login is `admin2@gmail.com` / `admin123`; the mock owner login is `owner@demo.local` / `Owner123!`. For password-change previews, the manager's current password is `admin123` and both sample staff accounts use `staff123`. Only the owner session can use the mock password-change and permanent-delete actions.

To inspect the app without any backend, use either debug-only offline preview login shown on the sign-in page: `demo@offline.local` / `Preview123!` for the normal user navigation, or `owner@offline.local` / `OwnerPreview123!` to preview the owner Admin tab and user details. The offline user directory uses `Preview123!` as each non-owner account's current password. Offline password changes and deletions affect only the current app process. These credentials are not accepted by release builds, and offline preview does not call the API.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
