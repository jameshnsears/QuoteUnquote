# Quote Unquote

<a href="https://play.google.com/store/apps/details?id=com.github.jameshnsears.quoteunquote&hl=en"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png" height="75"/></a> [<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75" alt="Get it on F-Droid">](https://f-droid.org/packages/com.github.jameshnsears.quoteunquote/)

--- 

## 1. Status

|                                                                              | ci                                                                                                                                                                                                                      | codecov                                                                                                                                                                                           | codacy                                                                                                                                                                                                                                                                                                                   | sonarcloud                                                                                                                                                                                                                           | codescene                                                                                                                | deployment                                                                                                                                                                                                           |
|------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| :app                                                                         | codecov.yml                                                                                                                                                                                                             | codecov.yml                                                                                                                                                                                       | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/0d6227a494f747439d748802ca595999)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote&amp;utm_campaign=Badge_Grade)                                       |                                     | [![CodeScene general](https://codescene.io/images/analyzed-by-codescene-badge.svg)](https://codescene.io/projects/20353) |                                                                                                                                                                                                                      |
| [:cloudLib](https://github.com/jameshnsears/QuoteUnquote.cloudLib)           | [![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib/actions/workflows/coverage.yml)                     | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib/branch/main/graph/badge.svg?token=hjNc1SbSgT)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib)                     | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/78d7a9a166b9420b9dc47991ef7cb028)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib&amp;utm_campaign=Badge_Grade)                     | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib)                     |                                                                                                                          |                                                                                                                                                                                                                      |
| [functions](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions) | [![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions/workflows/coverage.yml) | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions/branch/main/graph/badge.svg?token=jc55AxH2ry)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/5c0ebcf94aac443a8637460cf1a4068b)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib.functions/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib.functions&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib.functions&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib.functions) | [![CodeScene general](https://codescene.io/images/analyzed-by-codescene-badge.svg)](https://codescene.io/projects/20356) | [![deploy-gcp](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/workflows/deploy-gcp/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions?query=workflow%3Adeploy-gcp) |
| [:utilsLib](https://github.com/jameshnsears/QuoteUnquote.utilsLib)           | [![coverage](https://github.com/jameshnsears/QuoteUnquote.utilsLib/actions/workflows/coverage.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.utilsLib/actions/workflows/coverage.yml)                     | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib/branch/main/graph/badge.svg?token=UmWdOTiqB7)](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib)                     | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/e9cd947f7acf4a5cb090d49a09a7df3f)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.utilsLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.utilsLib&amp;utm_campaign=Badge_Grade)                     | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.utilsLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.utilsLib)                     |                                                                                                                          |                                                                                                                                                                                                                      |

### 1.1. codecov.yml

| test                                                                                                                                                                                             | androidTest                                                                                                                                                                                                           | cli (API 30)         | codecov                                                                                                                                                     |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [![coverage-test](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-test.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-test.yml) | [![coverage-androidTest](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-androidTest.yml/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions/workflows/coverage-androidTest.yml) | post-push-codecov.sh | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote/branch/main/graph/badge.svg?token=kzC7tE8QO8)](https://codecov.io/gh/jameshnsears/QuoteUnquote) |

---

## 2. High Level Design

### 2.1. Deployment Diagram

![Deployment Diagram](https://github.com/jameshnsears/QuoteUnquote/blob/main/docs/uml/Deployment%20Diagram.jpg?raw=true)

### 2.2. androidTest ROOM Class Diagram

![androidTest ROOM Class Diagram](https://github.com/jameshnsears/QuoteUnquote/blob/main/docs/uml/androidTest%20ROOM%20Class%20Diagram.jpg?raw=true)

---

## 3. Build Instructions

After cloning extract .gpg files - BuildConfig / GitHub Action Secrets values.

The app can be built on Windows 11 but the ./bin folder contains bash scripts (that work with git
bash; some require gh cli).

### 3.1. CLI

```text
git clone https://github.com/jameshnsears/QuoteUnquote
cd QuoteUnquote
git submodule update --init --recursive
git submodule foreach git pull origin main
git submodule
```

### 3.2. Android Studio

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

### 3.3. PyCharm

```text
After a clone...

PyCharm > Open > QuoteUnquote.cloudLib.functions

File > Settings > Project:
 > set Intepreter > New Virtualenv Environment

Virtualenv Terminal > 
 > python -m pip install -r requirements-test.txt
 > python -m pip install -r src/requirements.txt

run a Run/Debug Configuration 
 > setting Python Intepreter to Virtualenv Environment
```
