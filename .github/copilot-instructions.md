# Copilot Instructions for QuoteUnquote

This document helps Copilot sessions work effectively in the QuoteUnquote repository.

## Architecture Overview

Quote Unquote is a multi-module Android application with optional cloud backend:

- **`app/`** — Main Android app with UI, local persistence (Room/SQLite), widgets, and cloud sync integration
- **`QuoteUnquote.utilsLib/`** — Shared Android utilities (preferences, notifications, widget helpers)
- **`QuoteUnquote.cloudLib/`** — Android library defining cloud transfer data models and endpoint configuration
- **`QuoteUnquote.cloudLib.functions/`** — Python backend for cloud sync/backup using Flask and Firestore

The app works primarily offline but supports optional cloud sync for favourites and full app state transfer via code-based transfer keys.

## Build & Test Commands

### Prerequisites

After cloning, initialize submodules:
```bash
git submodule update --init --recursive
git submodule foreach git pull origin main
```

Decrypt secrets (requires GPG):
```bash
# Creates local.properties from encrypted .gpg files
# (Done manually in CI via secrets)
```

### Android (Gradle)

All commands use the wrapper: `./gradlew [task]`

**Unit Tests** (runs locally, not on device):
```bash
# Run both build flavors
./gradlew :app:testGooglePlayDebugUnitTestCoverage
./gradlew :app:testFdroidDebugUnitTestCoverage

# Single flavor
./gradlew :app:testDebugUnitTest          # Current variant
./gradlew :app:test --stacktrace          # With details
```

**Android Instrumentation Tests** (emulator/device):
```bash
# Covered by CI workflow, generally not run locally
./gradlew connectedAndroidTest
```

**Static Analysis** (all run independently):
```bash
./gradlew checkstyle              # Code style (sun_checks.xml rules)
./gradlew detekt                  # Code quality & rules
./gradlew ktlint                  # Kotlin formatting
./gradlew lintGooglePlayDebug     # Android lint (requires google-services.json)
./gradlew lintFdroidDebug         # Android lint (F-Droid build)
```

**Build**:
```bash
./gradlew assemble{FdroidDebug,GoogleplayDebug}        # APK
./gradlew assemble{FdroidRelease,GoogleplayRelease}    # Production (requires signing config)
./gradlew :app:build              # Full build with tests
```

**Clean Gradle Cache** (if cache issues):
```bash
./ci_clear_gradle_cache.sh
```

### Python Backend (`QuoteUnquote.cloudLib.functions/`)

**Unit Tests & Coverage**:
```bash
cd QuoteUnquote.cloudLib.functions
python -m pytest --cov=src test/        # Run with coverage
python -m pytest test/[test_name].py    # Single test file
python -m pytest test/ -v               # Verbose output
```

**Linting & Code Quality**:
```bash
python -m pylint src/                   # Pylint
python -m flake8 src/                   # Flake8
python -m bandit -r src/blueprint       # Security check
python -m ruff check src/               # Ruff linter
```

**Local Development** (Flask):
```bash
cd QuoteUnquote.cloudLib.functions
python -m pip install -r requirements-test.txt
python -m pip install -r src/requirements.txt
python src/index.py                     # Runs on http://localhost:8080
```

## Key Conventions

### Android App Structure

**Package organization** in `app/src/main/kotlin/com/github/jameshnsears/quoteunquote/`:
- `configure/` — Configuration UI and preference fragments
- `cloud/` — Cloud sync and transfer workflows
- `db/` — Room database models, DAOs, and local persistence
- `scraper/` — Quote parsing and import logic
- `sync/` — Sync state validation
- `utils/` — App-level helpers and preference access

**Build flavors** (in `build.gradle`):
- `fdroid` — F-Droid build (no Firebase/Google Play dependencies)
- `googleplay` — Google Play build (includes Firebase, Google Services)
- `espresso` — Testing flavor with mocked dependencies
- `uiautomator` — UI automation testing flavor

**Database**: Room migrations are tracked in `app/schemas/`; use `@Database(version=X)` and provide migration paths.

**Compose UI**: The app uses Jetpack Compose for UI; Layout composition follows standard Compose patterns (state hoisting, modifier chains).

### Python Backend Structure

**Endpoints** (in `src/endpoint/`):
- `POST /save` → Save favourites by transfer code
- `POST /receive` → Retrieve favourites by transfer code
- `POST /transfer_backup` → Save full transfer payload
- `POST /transfer_restore` → Retrieve full transfer payload

**Cloud integration** (in `src/cloud/gcp/`):
- `gcp_firestore.py` — Firestore read/write operations
- `gcp_logging.py` — Cloud Logging integration

**Validation** (in `src/validation/`): Request validation before Firestore operations

**Storage adapters** (in `src/storage/`): Data serialization and Firestore document mapping

**Testing**: Tests in `test/` mirror endpoint structure (e.g., `test/favourites/`, `test/transfer/`); use `conftest.py` for fixtures.

### Kotlin & Java Style

- **Language**: Kotlin preferred; Java only for legacy/legacy integration
- **Min SDK**: 26 (Android 8)
- **Target SDK**: 36 (Android 15)
- **Java**: 17 (preview features disabled)
- **Formatting**: ktlint (automatic via `./gradlew ktlint -F` to fix)
- **Compose Compiler**: Enabled for Compose support

### Secrets & Configuration

**local.properties** (Git-ignored, decrypted from `.gpg` files in CI):
- Android signing keystore credentials
- Build configuration flags

**google-services.json** (Google Play flavor only):
- Firebase and Google Play configuration
- Decrypted in CI; F-Droid build skips this entirely

**Modules use separate local.properties**:
- Root: `local.properties`
- cloudLib: `QuoteUnquote.cloudLib/cloudLib/local.properties`
- utilsLib: `QuoteUnquote.utilsLib/utilsLib/local.properties`

### Code Quality Gates

**Linting Rules**:
- Checkstyle (`sun_checks.xml`): Standard Java style checks
- Detekt: Kotlin code quality and complexity rules
- ktlint: Kotlin formatting (auto-fixable)
- Android Lint: Platform-specific issues

**Coverage**:
- Instrumented with JaCoCo for both unit and Android tests
- Reports uploaded as workflow artifacts (codecov.io disabled; see README)

**Pre-push Hook**: `bin/pre-push-static-analysis.sh` runs local static checks (optional).

### Test Categories

**Unit Tests** (`src/test/`):
- Run locally without device/emulator
- Include ViewModel tests, utility tests, and business logic
- Faster feedback loop

**Instrumented Tests** (`src/androidTest/`):
- Run on emulator/device
- Include UI tests (Espresso), database tests, integration tests
- Separate flavors for espresso and uiautomator testing

**Python Tests** (`QuoteUnquote.cloudLib.functions/test/`):
- Use pytest + unittest.mock
- Organized by endpoint (favourites, transfer)
- Mock Firestore and cloud logging

## Development Workflow

1. **Create feature branch** from `main`
2. **Run local linting & tests** before committing:
   - `./gradlew checkstyle detekt ktlint`
   - `./gradlew :app:testDebugUnitTest`
   - For Python: `python -m pytest test/` in `QuoteUnquote.cloudLib.functions/`
3. **Stage changes** and check CI workflows:
   - `static-analysis.yml` — Runs checkstyle, detekt, ktlint, and Android lint
   - `coverage-test.yml` — Runs unit tests for both flavors
   - `coverage-androidTest.yml` — Runs instrumented tests (on emulator)
4. **Push to feature branch** for PR
5. **CI validates** all checks before merge to main

## Submodules

The repository uses Git submodules for shared Android libraries:
- `QuoteUnquote.cloudLib` → defines cloud transfer models
- `QuoteUnquote.utilsLib` → defines shared Android utilities
- `QuoteUnquote.cloudLib.functions` → Python backend (separate GitHub Actions)

**Submodule updates**:
```bash
git submodule update --recursive --remote   # Update all to latest main
git submodule foreach git pull origin main  # Pull specific branch
```

When a submodule changes in the main repo, update the main `.gitmodules` to track the new commit.

## Common Tasks

### Running a Single Test

**Android (Kotlin)**:
```bash
./gradlew -p app test -k "SpecificTestClass"     # By class name
./gradlew test --tests "com.github.jameshnsears.quoteunquote.db.*"  # By package
```

**Python**:
```bash
cd QuoteUnquote.cloudLib.functions
python -m pytest test/favourites/test_favourites.py::test_save -v
```

### Adding a New Unit Test

1. Create test file in `src/test/kotlin/` or `src/test/java/` (Android)
2. Import test fixtures and use standard JUnit or pytest
3. Run locally: `./gradlew test`
4. Verify coverage in `build/reports/jacoco/`

### Debugging

**Android Studio**:
- Set breakpoints in code
- Use Run > Debug to step through tests or app

**Python**:
```bash
python -m pdb -m pytest test/favourites/test_favourites.py
```

## CI/CD Pipeline

**Workflows** (in `.github/workflows/`):
- `static-analysis.yml` — Runs on all branches (checkstyle, detekt, ktlint, linting)
- `coverage-test.yml` — Unit tests with JaCoCo coverage
- `coverage-androidTest.yml` — Instrumented tests on emulator
- `static-sonarqube.yml` — SonarQube quality gate
- `static-gitleaks.yml` — Secret scanning
- `release-github-fdroid.yml` — F-Droid release automation

**Build Variants**:
- Tests run for both `fdroid` and `googleplay` flavors independently
- Instrumented tests use espresso and uiautomator runners

## Resources

- **Product Context**: See `PRODUCT.md` for user-facing features
- **Architecture Deep-Dive**: See `ARCHITECTURE.md` for module responsibilities
- **Build Config**: Root `build.gradle` defines shared dependencies; modules use individual `build.gradle`
- **Gradle Dependencies**: Managed via version catalog (check `gradle/libs.versions.toml` or similar)
