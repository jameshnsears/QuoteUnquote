# Quote Unquote

<a href="https://play.google.com/store/apps/details?id=com.github.jameshnsears.quoteunquote&hl=en"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png" height="75"/></a> [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75" alt="Get it on F-Droid">](https://f-droid.org/packages/com.github.jameshnsears.quoteunquote/)

--- 

## 1. Status - GitHub Actions

NOTE: coverage not shown as codecov.io now requires payment for flags feature. See action upload artifacts for jacoco files.

### :app

[![static-analysis](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/static-analysis.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/static-analysis.yml)

[![static-sonarqube](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/static-sonarqube.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/static-sonarqube.yml)


[![coverage-test](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-test.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-test.yml)

[![coverage-androidTest](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-androidTest.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-androidTest.yml)

### :cloudLib
[![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib/actions/workflows/coverage.yml)

### :cloudLib.functions
[![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions/workflows/coverage.yml)


### :utilsLib
[![coverage](https://github.com/jameshnsears/QuoteUnquote.utilsLib/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.utilsLib/actions/workflows/coverage.yml)

---

## 2. Build Instructions

After cloning I extract .gpg files (BuildConfig, GitHub Action Secrets values) to create
local.properties

The app can be built on Windows 11 but the ./bin folder contains bash scripts (that work with git
bash; some require gh cli).

### 2.1. CLI

```text
git clone https://github.com/jameshnsears/QuoteUnquote
cd QuoteUnquote
git submodule update --init --recursive
git submodule foreach git pull origin main
git submodule
```

#### 2.1.1 CLI update from secondary source

```text
cd QuoteUnquote
git pull
git submodule update --recursive --remote
```

### 2.2. Android Studio

```text
Android Studio > Get from Version Control
 > https://github.com/jameshnsears/QuoteUnquote
 > Clone

NOTE: import to clone for IDE as submodules maintained

VCS > Git > Branches...
 > Remote Branches > origin/main > Checkout
  > Smart Checkout

git submodule update --remote

git remote add origin/cloudLib https://github.com/jameshnsears/QuoteUnquote.cloudLib
git remote add origin/cloudLib.functions https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions
git remote add origin/utilsLib https://github.com/jameshnsears/QuoteUnquote.utilsLib

Build > Rebuild Project

Choose a Build Variant  
 > run a Run/Debug Configuration
```

### 2.3. PyCharm

```text
After a clone...

PyCharm > Open > QuoteUnquote.cloudLib.functions

File > Settings > Project:
 > set Interpreter > New Virtualenv Environment

Virtualenv Terminal > 
 > python -m pip install -r requirements-test.txt
 > python -m pip install -r src/requirements.txt

run a Run/Debug Configuration 
 > setting Python Interpreter to Virtualenv Environment
```
