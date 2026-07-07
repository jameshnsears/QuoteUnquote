# Quote Unquote — CLAUDE.md

## Project Overview

A highly customizable Android App Widget that displays quotations. Users can personalize widget appearance (colors, fonts, transparency, layouts), select quote sources (internal database, CSV import, web-scraped), receive daily/interval notifications, and use cloud backup/restore.

- **Package**: `com.github.jameshnsears.quoteunquote`
- **Version**: 4.56.0 (code 1973036)
- **Play Store**: [Quote Unquote on Google Play](https://play.google.com/store/apps/details?id=com.github.jameshnsears.quoteunquote)
- **F-Droid**: Available on F-Droid

## Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Language | Kotlin + Java (migrating to Kotlin) | Kotlin 2.4.0 |
| Build | Gradle + AGP | Gradle 9.6.1 / AGP 9.2.1 |
| JDK | Java 17 | |
| Min SDK | 30 | |
| Target / Compile SDK | 37 / 37 | |
| UI | Jetpack Compose (Material 3) + XML Views | Compose BOM 2026.06.01 |
| Database | Room | |
| Cloud | Firebase (googleplay flavor only) | |

## Module Structure

- **`:app`** — Main Android app (widget, config, sync, scraper, notifications)
- **`:cloudLib`** — Android library for cloud sync/data transfer (submodule: `QuoteUnquote.cloudLib/`)
- **`:utilsLib`** — Shared utilities library (submodule: `QuoteUnquote.utilsLib/`)
- **QuoteUnquote.cloudLib.functions/** — Python cloud backend (submodule)

All library modules are git submodules.

## Build Variants

Three flavors in the `Version` dimension:

| Flavor | Suffix | Notes |
|---|---|---|
| `googleplay` | `-googleplay` | Firebase services, release signing |
| `fdroid` | `-fdroid` | Default flavor, no Firebase |
| `espresso` | `-espresso` | Instrumented test target |
| `uiautomator` | `-uiautomator` | UI Automator test target |

Build types: `debug`, `release`, `benchmark`

## Key Conventions

### Code Quality
- **Spotless** (Ktlint): `./gradlew spotlessKotlinCheck` / `spotlessApply`
- **Detekt**: `./gradlew detekt` (config in `config/detekt/detekt.yml`)
- **Checkstyle**: `./gradlew checkstyle` (config in `config/checkstyle/`)
- **JaCoCo** coverage reports generated per flavor+buildType combination
- **Formatting**: 4-space indent, 140 max line length, UTF-8, LF line endings (`.editorconfig`)

### Code Style
- Mixed Java/Kotlin codebase; new code in Kotlin
- Java in `src/main/java/`, Kotlin in `src/main/kotlin/`
- XML layouts heavily used in configuration fragments
- Jetpack Compose for newer UI components
- ViewBinding and DataBinding for XML layouts

### Testing
- **Unit tests**: `app/src/test/kotlin/` — JUnit tests
- **Instrumentation tests**: `app/src/androidTest/` — Espresso + UI Automator
- Test resources in `app/src/test/resources/`

### Dependency Management
- Version catalog (`libs.versions.toml`) via Gradle
- Submodules for library projects: `QuoteUnquote.cloudLib`, `QuoteUnquote.utilsLib`, `QuoteUnquote.cloudLib.functions`

## CI/CD

GitHub Actions workflows in `.github/workflows/`:
- `static-analysis.yml` — Spotless + Detekt + Checkstyle
- `static-gitleaks.yml` — Secret scanning
- `coverage-test.yml` — Unit test coverage
- `coverage-androidTest.yml` — Instrumented test coverage
- `release-fdroid_rc.yml` — F-Droid release builds

## Project Conventions

- Code style: Kotlin official style (`kotlin.code.style=official` in `gradle.properties`)
- Configuration-cache enabled in Gradle
- `.aiexclude` excludes `build/`, `.gradle/`, `.kotlin/` from AI analysis

## Development Commands

```bash
# Build & test
./gradlew :app:assembleFdroidDebug       # Build FDroid debug APK
./gradlew :app:testFdroidDebugUnitTest   # Run unit tests
./gradlew :app:spotlessKotlinApply       # Format Kotlin code
./gradlew :app:detekt                    # Static analysis
./gradlew :app:checkstyle                # Checkstyle analysis
```

# Claude Code Project Rules

## Context Management & Token Savings
- NEVER read, grep, or index files inside the `**/build/` or `**/.gradle/` directories.
- Generated Android artifacts (such as Room schemas or Hilt/Dagger generated code) must be ignored unless explicitly asked by the user.
- If a build fails, do not ingest the entire Gradle stack trace. Only read the specific compiler error message provided in the terminal.

## Code Fix & Quality Standards

*   **Deprecation Resolution**: For any code fix or modification, you MUST proactively identify and resolve any deprecated API usages within the touched files to ensure the codebase remains modern.
*   **Verification**: Every code fix MUST be accompanied by a unit test (or updates to existing tests) that explicitly proves the fix worked and prevents regressions.
