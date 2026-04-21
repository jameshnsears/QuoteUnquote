# Quote Unquote Product Overview

## What it is

Quote Unquote is an Android quotations and affirmations app with home screen widget support, rich customization, and optional cloud-backed sync/restore.

It is designed for users who want daily inspiration from curated quotes, quick access to source details, and the ability to preserve or transfer their app settings and favourite selections.

## Core user value

Quote Unquote delivers:
- an easy-to-use quote widget for home screen inspiration
- offline access to a local quote database
- configurable display and notification behavior
- support for favourites, search, and quote browsing
- both local and remote backup/restore for user data

## Primary product capabilities

### 1. Home widget and daily quotes

- Provides one or more app widgets that display quotations on the Android home screen.
- Widgets can be refreshed manually or automatically to show a new quote.
- The widget experience is central to the product, with quick access to quote details and source metadata.

### 2. Quote browsing and search

- Users can browse quotations stored in the local app database.
- Search functionality filters quotes by author, content, or custom criteria.
- Users can manage and edit quoted content via the app UI.

### 3. Favourites and custom selection

- Users can mark quotes as favourites for easy retrieval.
- The app tracks favourite counts and surface them in the widget or app interface.
- Favourite content is included in remote transfer and backup workflows.

### 4. Appearance and personalization

- Extensive appearance settings let users control text style, colors, fonts, and layout.
- Users can configure how quotes display in the widget and app screens.
- The product supports multiple UI themes and preference-driven customization.

### 5. Notifications and reminders

- Timely notifications can surface quotes or reminders to the user.
- Notification settings are adjustable so users can receive the right cadence of content.

### 6. Import and external quote sources

- The app supports importing external quotations from files or web-scraped sources.
- This enables users to extend the built-in quote library with custom collections.

### 7. Backup, restore, and device transfer

- Local backup/restore is supported so users can preserve their quote database, favourites, and settings.
- Remote backup and transfer workflows enable data portability between devices.
- A code-based transfer mechanism lets users save and restore app state through a cloud backend.

## Cloud sync support

Quote Unquote includes optional cloud services built around remote transfer and favourites storage:
- remote favourites persistence
- full transfer backup/restore of app state
- dedicated backend endpoints for save/receive and transfer operations

These capabilities are implemented in the separate `QuoteUnquote.cloudLib.functions` module and are optional for users who want remote device continuity.

## Audience and usage scenarios

### Daily inspiration
Users who want a simple home screen widget that shows a new quote regularly and offers one-tap access to source details.

### Personal curation
Users who want to build a custom collection of favourite quotations and control which quotes appear in notifications or widgets.

### Device continuity
Users who want to move their quote settings, favourites, and backup data between devices, or recover after reinstalling the app.

## Product differentiators

- widget-first experience with rich customization
- support for both Google Play and F-Droid builds
- offline functionality as the default mode
- remote backup/restore for quote collections and settings
- integration with curated quotes, external imports, and search

## Release and distribution

- Main Android app module: `app/`
- Shared utilities module: `QuoteUnquote.utilsLib/utilsLib/`
- Cloud transfer data model: `QuoteUnquote.cloudLib/cloudLib/`
- Cloud backend service: `QuoteUnquote.cloudLib.functions/`

The product targets Android users who value inspirational quotes, customizable widget experiences, and reliable data portability.
