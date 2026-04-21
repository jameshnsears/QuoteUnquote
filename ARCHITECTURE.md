# Quote Unquote Architecture

## Overview

Quote Unquote is a multi-module repository centered on an Android quoting app with local persistence, widget support, and optional cloud sync/backup capabilities.

The repository is organized as:
- `app/` — main Android application
- `QuoteUnquote.utilsLib/utilsLib/` — shared Android utility library
- `QuoteUnquote.cloudLib/cloudLib/` — Android library for cloud transfer models and remote device endpoint integration
- `QuoteUnquote.cloudLib.functions/` — Python serverless backend for remote sync and transfer

## High-level Architecture

The app is an Android client that maintains quote data locally and can communicate with a cloud backend for sharing favourites and transferring app state.

    [Android App `app/`]
            │
            │ uses
            ▼
    [Android Libraries]
      ├─ `utilsLib` (common utilities)
      └─ `cloudLib` (cloud transfer data models, remote endpoint config)
            │
            │ exchanges JSON/HTTP
            ▼
    [Serverless Backend `QuoteUnquote.cloudLib.functions/`]
      ├─ Flask-powered HTTP endpoints
      ├─ Firestore persistence
      └─ request validation + logging

## Modules

### `app/`

This is the Android application module.

Key responsibilities:
- User interface for quote browsing, configuration, search, favourites, and widget control
- Local persistence via Room/SQLite and support for external quotation files
- Android home screen widget integration
- Cloud sync/backup actions for:
  - favourites persistence
  - full transfer backup/restore
- Product flavors for `googleplay`, `fdroid`, `espresso`, and `uiautomator`
- Build configuration using Kotlin, AndroidX Compose, Java 17 compatibility, WorkManager, and Firebase Crashlytics

Relevant package groups:
- `com.github.jameshnsears.quoteunquote.configure` — UI configuration screens and fragments
- `com.github.jameshnsears.quoteunquote.cloud` — cloud interaction and sync workflows
- `com.github.jameshnsears.quoteunquote.db` — local database and history management
- `com.github.jameshnsears.quoteunquote.scraper` — quote scraping and parsing
- `com.github.jameshnsears.quoteunquote.sync` — sync state and validation
- `com.github.jameshnsears.quoteunquote.utils` — app-level helpers and preferences

### `QuoteUnquote.utilsLib/utilsLib/`

This module provides reusable Android utility classes and shared platform code.

Key features:
- common helper functions and intent builders
- notification and widget support utilities
- preferences and settings handling
- optional App Center / Crashlytics support via build config

### `QuoteUnquote.cloudLib/cloudLib/`

This library module models cloud transfer payloads and remote device sync data.

Key responsibilities:
- defining the JSON contract for transfer payloads (`Transfer`, `Quotations`, `Settings`, `Schedule`, `Sync`, etc.)
- serializable data classes annotated for Gson
- environment-specific endpoint configuration using `REMOTE_DEVICE_ENDPOINT_*`
- support for remote device and backup/restore workflows that the Android app consumes

### `QuoteUnquote.cloudLib.functions/`

This module contains the server-side component used for remote backup and favourite transfer.

Key architecture elements:
- Python 3.11 serverless code
- Flask-based local app entrypoint for development (`src/index.py`)
- Google Cloud Functions entrypoints in `src/main.py`
- Firestore access in `src/cloud/gcp/gcp_firestore.py`
- request adapters in `src/storage/*`
- validation in `src/validation/*`
- Cloud Logging integration via `src/cloud/gcp/gcp_logging.py`

Endpoints:
- `/save` → persist favourites by code
- `/receive` → retrieve favourites by code
- `/transfer_backup` → persist full transfer payload by code
- `/transfer_restore` → retrieve transfer payload by code

Persistence:
- Firestore collection `favourites_collection`
- Firestore collection `transfer_collection`

## Data Flow

### Local app data
- The Android app keeps quotes, preferences, and widget state locally.
- Data passes through Room/SQLite and optional external file import/export routines.

### Cloud sync / backup
- The app serializes cloud transfer payloads using types from `cloudLib`
- A code-based transfer key is used to store or retrieve data remotely
- The backend validates requests and writes to Firestore
- The same cloud backend supports both favourites sync and full app transfer/restore

### Shared utilities
- Common helpers and configuration live in `utilsLib`
- These utilities reduce duplication across the Android app and library components

## Build and Deployment

### Android
- Multi-module Gradle project with shared root configuration in `build.gradle`
- App module uses Android Gradle Plugin and Kotlin Compose
- `settings.gradle` includes `:app`, `:cloudLib`, and `:utilsLib`

### Python backend
- Managed separately under `QuoteUnquote.cloudLib.functions/`
- Uses `requirements-test.txt` and `src/requirements.txt`
- Deployable as Google Cloud Functions or Cloud Run

## Cross-cutting concerns

- static analysis with Detekt, ktlint, Checkstyle, and SonarQube
- code coverage instrumentation via JaCoCo
- build variants to support F-Droid and Google Play releases

## Key Design Principles

- separation of presentation, persistence, and sync layers
- modular reuse for cloud payload models and shared helpers
- lightweight backend focused on Firestore persistence and request validation
- Android-first experience with optional remote backup/sync capability
