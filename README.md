# Quote, Unquote... a Quotations Widget

<a href="https://play.google.com/store/apps/details?id=com.github.jameshnsears.quoteunquote&hl=en"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png" height="70"/></a>

---

## 1. Status

| (sub)module | branch | ci:coverage | codecov | codacy | sonarcloud | deployment |
| - | - |- | - | - | - | - |
| :app | development | [![coverage](https://github.com/jameshnsears/QuoteUnquote/workflows/coverage/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions?query=workflow%3Acoverage) | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote/branch/development/graph/badge.svg?token=MUVXyY6kDV)](https://codecov.io/gh/jameshnsears/QuoteUnquote) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/54a839ac6f6c4154b746592439e4b894)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote) | [![deploy-appcenter](https://github.com/jameshnsears/QuoteUnquote/workflows/deploy-appcenter/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions?query=workflow%3Adeploy-appcenter) |
| [:cloudLib](https://github.com/jameshnsears/QuoteUnquote.cloudLib) | master | [![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib/workflows/coverage/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib/actions?query=workflow%3Acoverage) | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib/branch/master/graph/badge.svg?token=hjNc1SbSgT)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/fdfcc4b00ba74534955a071646f20250)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib) | |
| [:utilsLib](https://github.com/jameshnsears/QuoteUnquote.utilsLib) | master | [![coverage](https://github.com/jameshnsears/QuoteUnquote.utilsLib/workflows/coverage/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.utilsLib/actions?query=workflow%3Acoverage) | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib/branch/master/graph/badge.svg?token=UmWdOTiqB7)](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/8dcaedafebe249229714533096414aba)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.utilsLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.utilsLib&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.utilsLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.utilsLib) | |
| [functions](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions) | master | [![coverage](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/workflows/coverage/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions?query=workflow%3Acoverage) | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions/branch/master/graph/badge.svg?token=jc55AxH2ry)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/5c0ebcf94aac443a8637460cf1a4068b)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib.functions/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib.functions&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib.functions&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib.functions) | [![deploy-gcp](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/workflows/deploy-gcp/badge.svg)](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/actions?query=workflow%3Adeploy-gcp) |

* :app combined /test + /androidTest codecov also available via [coverage-codecov.sh](https://raw.githubusercontent.com/jameshnsears/QuoteUnquote/development/bin/coverage-codecov.sh) - as CI emulator not 100% reliable.

---

## 2. High Level Design

### 2.1. Deployment Diagram

![Deployment Diagram](https://github.com/jameshnsears/QuoteUnquote/blob/development/docs/Deployment%20Diagram.jpg?raw=true)

### 2.2. androidTest ROOM Class Diagram

![androidTest ROOM Class Diagram](https://github.com/jameshnsears/QuoteUnquote/blob/development/docs/androidTest%20ROOM%20Class%20Diagram.jpg?raw=true)

---

## 3. Build Instructions

### 3.1. Android Studio

```text
Android Studio > Get from Version Control
 > https://github.com/jameshnsears/QuoteUnquote
 > Clone

NOTE: import to clone for IDE as submodules maintained

VCS > Git > Branches...
 > Remote Branches > origin/development > Checkout
  > Smart Checkout

git submodule update --remote

extract/overwrite local.properties from .pgp files for :app + modules = BuildConfig / GitHub Action Secrets values

File > New > Import Module...
 >  :cloudLib
...both modules get imported by Android Studio + settings.gradle might duplicate the import!

git remote add cloudLib https://github.com/jameshnsears/QuoteUnquote.cloudLib
git remote add utilsLib https://github.com/jameshnsears/QuoteUnquote.utilsLib

Build > Rebuild Project

Choose a Build Variant  
 > run a Run/Debug Configuration
```

### 3.2. PyCharm

```text
After a clone...

PyCharm > Open > QuoteUnquote.cloudLib.functions

File > Settings > Project:
 > set Intepreter > New Virtualenv Environment

Virtualenv Terminal > 
 > python -m pip install -r requirements-test.txt
 > python -m pip install -r src/requirements.txt

extract three .pgp files = BuildConfig / GitHub Action Secrets values

run a Run/Debug Configuration 
 > setting Python Intepreter to Virtualenv Environment
```
