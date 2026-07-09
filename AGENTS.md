# QuoteUnquote - AI Agent Guide

This document serves as the authoritative guide for AI coding agents (Gemini, Codex, Claude Code, etc.) working on the QuoteUnquote repository.

---

## 1. Project Overview

*   **Purpose**: An Android application centered around a highly customizable App Widget that displays quotations.
*   **Primary Functionality**:
    *   Displaying quotes on the home screen via widgets.
    *   Customization of widget appearance (colors, fonts, transparency, layouts).
    *   Selection of quote sources (internal database, user-imported, web-scraped).
    *   Daily or interval-based notifications.
    *   Cloud backup and restore functionality.
    *   Search and Favorites management.
*   **Target Users**: Android users who want inspiration or information via quotations on their home screen.
*   **Architecture**: Transitioning from a monolithic Java structure with a thick Model layer to a more modular Kotlin-based architecture. It currently uses a "Model-Repository-DAO" pattern, heavily centered around the `AppWidgetProvider` lifecycle.
*   **Current Development Status**: Actively maintained, migrating to Kotlin and Compose, upgrading to latest Android SDKs (target 36, compile 37).

---

## 2. Repository Structure

The project is structured into three main Gradle modules:

*   **`app/`**: The main Android application module. Contains the widget provider, configuration fragments, and application logic.
    *   `src/main/java/`: Legacy Java source code (Models, DAOs, Widget Provider).
    *   `src/main/kotlin/`: Modern Kotlin source code (Sync, Scraper, newer UI components).
    *   `src/main/res/`: Layouts (XML), Drawables, Values.
    *   `src/androidTest/`: Instrumentation tests (Espresso, UIAutomator).
*   **`QuoteUnquote.cloudLib/cloudLib/`**: Android library for cloud synchronization and data transfer.
*   **`QuoteUnquote.utilsLib/utilsLib/`**: Android library containing common utilities shared across modules.
*   **`QuoteUnquote.cloudLib.functions/`**: Python-based cloud backend/functions for synchronization.
*   **`config/`**: Contains configuration files for static analysis tools (**Detekt**, **Checkstyle**, **Lint**).
*   **`fastlane/`**: Contains metadata for store listings.
*   **`docs/`**: Project documentation, checklists, and release notes.

---

## 3. Technology Stack

*   **Kotlin**: 2.4.0
*   **AGP (Android Gradle Plugin)**: 9.2.1
*   **Gradle**: 9.6.1
*   **Java**: 17
*   **Android SDK**: Compile 37, Target 37, Min 31
*   **UI**:
    *   **Jetpack Compose**: Material 3 (1.4.0), BOM 2026.06.01. Used for newer UI parts.
    *   **XML Views**: Heavily used in configuration fragments (ViewBinding, DataBinding).
    *   **App Widgets**: Uses `RemoteViews` for the home screen widget.
*   **Reactive**: RxJava 2, RxAndroid, RxBinding. Used for database operations and event handling.
*   **Database**: Room (2.8.4). Multiple databases: Quotation, External, History, HistoryExternal.
*   **Background Work**: WorkManager (2.11.2) for cloud backups.
*   **Logging**: Timber (5.0.1).
*   **Networking**: OkHttp 5.4.0, Jsoup (1.22.2) for scraping.
*   **Firebase**: Crashlytics and Google Services for analytics and crash reporting.
*   **Dependency Injection**: Manual DI via Singleton patterns (e.g., `DatabaseRepository.getInstance()`).
*   **Testing**: JUnit 4, Mockk, Robolectric, Espresso, UIAutomator.

---

## 4. Architecture

*   **Model-Repository-DAO**:
    *   **Model**: `QuoteUnquoteModel.java` acts as the primary coordinator for data and business logic.
    *   **Repository**: `DatabaseRepository.java` abstracts access to multiple Room databases.
    *   **DAO**: Room DAOs handle low-level SQLite operations.
*   **UI Layer**:
    *   **AppWidget**: `QuoteUnquoteWidget.java` (AppWidgetProvider) handles widget lifecycle and updates.
    *   **Configuration**: Fragments (e.g., `AppearanceStyleFragment`) handle user settings.
*   **Data Flow**:
    *   User interacts with Widget or Config UI.
    *   UI calls `QuoteUnquoteModel`.
    *   `QuoteUnquoteModel` performs background work via `ExecutorService` and updates `DatabaseRepository`.
    *   Widget UI is updated via `RemoteViews` through `AppWidgetManager`.

---

## 5. Coding Standards

*   **Language**: Prefer Kotlin for new code. Maintain Java in existing modules until migrated.
*   **Naming**: Standard Android naming conventions (PascalCase for classes, camelCase for methods/variables, snake_case for resources).
*   **Formatting**: Enforced via **Spotless**, **ktlint**, **Checkstyle**, and **Detekt**.
*   **Nullability**: Use `@NonNull` and `@Nullable` annotations in Java. Use Kotlin's type system effectively.
*   **Coroutines**: Use Kotlin Coroutines for new asynchronous logic, though much legacy code uses RxJava or `ExecutorService`.
*   **Logging**: Use `Timber` for all logging.
*   **Resources**: Keep string resources localized in `strings.xml`. Use `plurals` and `string-array` where appropriate.

---

## 6. UI Guidelines

*   **Widgets**: Widgets use `RemoteViews`. Complex UI interactions should be handled via `PendingIntent`s.
*   **Fragments**: Use `ViewBinding` for XML-based fragments.
*   **Compose**: For new screens, follow Material 3 guidelines and proper state hoisting.
*   **Theming**: Supports Day/Night modes and system theme following. Themes are defined in `styles.xml` and `themes.xml`.

---

## 7. Accessibility & Localization

*   **TalkBack**: Ensure all `imageButton` elements have descriptive `contentDescription` attributes in the widget layout and configuration fragments.
*   **Touch Targets**: Maintain standard touch target sizes for toolbar buttons (at least 48dp).
*   **Localization**:
    *   All user-facing text must be stored in `strings.xml`.
    *   Support for multiple languages is managed via standard Android resource qualifiers (e.g., `values-es/strings.xml`).
    *   Use `plurals` for count-dependent strings and `string-array` for fixed lists (like font families).

---

## 8. Testing

*   **Unit Tests**: Located in `src/test`. Use `Robolectric` for Android-aware unit tests.
*   **Instrumentation Tests**: Located in `src/androidTest`. Use `Espresso` for UI testing and `UIAutomator` for cross-app/system interactions (like widget placement).
*   **Mocking**: Use `Mockk` for Kotlin and `Mockito` (if needed) for Java.
*   **Execution**:
    *   `./gradlew test`: Run all unit tests.
    *   `./gradlew connectedAndroidTest`: Run instrumentation tests on a device/emulator.
    *   `./gradlew detekt checkstyle ktlint spotlessCheck`: Run all static analysis and linting.

---

## 8. Build Instructions

*   **Clean**: `./gradlew clean`
*   **Build**: `./gradlew assembleDebug`
*   **Static Analysis**: `./gradlew detekt checkstyle ktlint spotlessCheck`
*   **Coverage**: `./gradlew jacocoTestReport` (or `testCombinedCoverage` tasks in `:app`)

---

## 9. CI/CD

*   **GitHub Actions**: Pipelines for static analysis, unit testing, instrumentation testing, and releases.
*   **Fastlane**: Used for managing Google Play Store metadata (`fastlane-googleplay/`).
*   **GPG**: Sensitive configuration files (keystores, google-services.json) are encrypted with GPG and decrypted during the build process.

---

## 10. Git Workflow

*   **Branching**: Feature branches merged into `master`/`main` via PRs.
*   **Commits**: Use descriptive commit messages. Signed commits are required for releases.

---

## 11. Agent Working Rules

*   **Architectural Consistency**: Do not change the `Model-Repository` pattern without significant justification.
*   **Minimalism**: Keep changes surgical. Avoid unnecessary refactoring unless specifically requested.
*   **Legacy Interop**: When modifying Java code, ensure compatibility with Kotlin callers and vice-versa.
*   **Test Updates**: Always update or add tests when changing business logic.
*   **Resource Management**: Check for existing strings or drawables before adding new ones.
*   **DI**: Follow the existing manual DI pattern (Singletons) unless instructed to introduce a DI framework like Hilt.

---

## 12. Common Development Tasks

*   **Adding a new Preference**:
    1. Update the corresponding `Preferences` class (e.g., `AppearancePreferences.java`).
    2. Add UI to the relevant Fragment XML and Java/Kotlin class.
    3. Update `QuoteUnquoteWidget` to handle the new preference during `onUpdate`.
*   **Adding a new Quote Source**:
    1. Update `ContentSelection` enum.
    2. Add logic to `DatabaseRepository` to handle the new selection type.
    3. Update `QuoteUnquoteModel` and `QuoteUnquoteWidget` for selection and display.

---

## 13. Specialized Skills (Agents)

This repository includes specialized AI agents (skills) to assist with specific development tasks. These are located in `.agents/skills/`.

| Skill | Description | Path |
|-------|-------------|------|
| **styles** | Integrate Jetpack Compose Styles API. | `.agents/skills/styles/SKILL.md` |
| **adaptive** | Make UI adapt to different devices. | `.agents/skills/adaptive/SKILL.md` |
| **android-cli** | Instructions for the `android` CLI. | `.agents/skills/android-cli/SKILL.md` |
| **r8-analyzer** | Optimize R8 keep rules. | `.agents/skills/r8-analyzer/SKILL.md` |
| **appfunctions** | Expose user workflows for AppFunctions. | `.agents/skills/appfunctions/SKILL.md` |
| **edge-to-edge** | Migrate to adaptive edge-to-edge support. | `.agents/skills/edge-to-edge/SKILL.md` |
| **navigation-3** | Migrate to Jetpack Navigation 3. | `.agents/skills/navigation-3/SKILL.md` |
| **perfetto-sql** | Translate intents to Perfetto SQL. | `.agents/skills/perfetto-sql/SKILL.md` |
| **agp-9-upgrade** | Upgrade to AGP version 9. | `.agents/skills/agp-9-upgrade/SKILL.md` |
| **testing-setup** | Create a testing strategy. | `.agents/skills/testing-setup/SKILL.md` |
| **verified-email** | Implement verified email retrieval. | `.agents/skills/verified-email/SKILL.md` |
| **wear-compose-m3** | Work with Wear OS Compose Material3. | `.agents/skills/wear-compose-m3/SKILL.md` |
| **camera1-to-camerax** | Migrate to CameraX. | `.agents/skills/camera1-to-camerax/SKILL.md` |
| **engage-sdk-integration** | Resolve Play Engage SDK issues. | `.agents/skills/engage-sdk-integration/SKILL.md` |
| **android-intent-security** | Android Intent security best practices. | `.agents/skills/android-intent-security/SKILL.md` |
| **perfetto-trace-analysis** | Analyze Perfetto traces. | `.agents/skills/perfetto-trace-analysis/SKILL.md` |
| **migrate-xml-views-to-jetpack-compose** | Structured workflow for XML to Compose migration. | `.agents/skills/migrate-xml-views-to-jetpack-compose/SKILL.md` |
| **play-billing-library-version-upgrade** | Upgrade Play Billing Library. | `.agents/skills/play-billing-library-version-upgrade/SKILL.md` |
| **display-glasses-with-jetpack-compose-glimmer** | Develop for display glasses using Glimmer. | `.agents/skills/display-glasses-with-jetpack-compose-glimmer/SKILL.md` |

---

## 14. Maintenance

This `AGENTS.md` should be updated whenever:
*   A new module is added.
*   A major dependency or tech stack change occurs (e.g., full migration to Compose or Hilt).
*   Coding standards or architecture patterns are significantly revised.

---

## 15. Glossary

*   **Digest**: A unique 8-character hex string identifying a quotation, usually derived from its author and content.
*   **WidgetId**: The unique ID assigned by Android to a specific instance of the app widget.
*   **Internal Database**: The read-only database of quotes shipped with the app.
*   **External Database**: The user-modifiable database for imported or scraped quotes.

---

## 16. Code Fix & Quality Standards

*   **Deprecation Resolution**: For any code fix or modification, you MUST proactively identify and resolve any deprecated API usages within the touched files to ensure the codebase remains modern.
*   **Verification**: Every code fix MUST be accompanied by a unit test (or updates to existing tests) that explicitly proves the fix worked and prevents regressions.
