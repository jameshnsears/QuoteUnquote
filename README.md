# Quote, Unquote... a Quotations Widget

<a href="https://play.google.com/store/apps/details?id=com.github.jameshnsears.quoteunquote&hl=en"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_gb/badges/images/generic/en_badge_web_generic.png" height="70"/></a>

---

## Status

| (sub)module | branch | codecov | codacy | sonarcloud | deployment |
| - | - |- | - | - | - |
| :app | development | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote/branch/development/graph/badge.svg?token=MUVXyY6kDV)](https://codecov.io/gh/jameshnsears/QuoteUnquote) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/54a839ac6f6c4154b746592439e4b894)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote) | [![deploy-appcenter](https://github.com/jameshnsears/QuoteUnquote/workflows/deploy-appcenter/badge.svg)](https://github.com/jameshnsears/QuoteUnquote/actions?query=workflow%3Adeploy-appcenter) |
| [:cloudLib](https://github.com/jameshnsears/QuoteUnquote.cloudLib) | master | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib/branch/master/graph/badge.svg?token=hjNc1SbSgT)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/fdfcc4b00ba74534955a071646f20250)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib) | |
| [:utilsLib](https://github.com/jameshnsears/QuoteUnquote.utilsLib) | master | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib/branch/master/graph/badge.svg?token=UmWdOTiqB7)](https://codecov.io/gh/jameshnsears/QuoteUnquote.utilsLib) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/8dcaedafebe249229714533096414aba)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.utilsLib/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.utilsLib&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.utilsLib&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.utilsLib) | |
| [functions](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions) | master | [![codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions/branch/master/graph/badge.svg?token=jc55AxH2ry)](https://codecov.io/gh/jameshnsears/QuoteUnquote.cloudLib.functions) | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/5c0ebcf94aac443a8637460cf1a4068b)](https://www.codacy.com/gh/jameshnsears/QuoteUnquote.cloudLib.functions/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jameshnsears/QuoteUnquote.cloudLib.functions&amp;utm_campaign=Badge_Grade) | [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jameshnsears_QuoteUnquote.cloudLib.functions&metric=alert_status)](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote.cloudLib.functions) | ![deploy-gcp](https://github.com/jameshnsears/QuoteUnquote.cloudLib.functions/workflows/deploy-gcp/badge.svg) |

---

## Build Instructions

### Android Studio

```text
Android Studio > Get from Version Control
 > https://github.com/jameshnsears/QuoteUnquote
 > Clone

VSC > Git > Branches...
 > Remote Branches > origin/development > Checkout
  > Smart Checkout

git submodule update --remote

extract/overwrite local.properties from .pgp files for :app + modules

File > New > Import Module...
 >  :cloudLib
...both modules get imported by Android Studio!

git remote add cloudLib https://github.com/jameshnsears/QuoteUnquote.cloudLib
git remote add utilsLib https://github.com/jameshnsears/QuoteUnquote.utilsLib

Build > Rebuild Project

Choose a Build Variant  
 > run a Run/Debug Configuration 
```

### PyCharm

```text
After a clone...

PyCharm > Open > QuoteUnquote.cloudLib.functions

File > Settings > Project:
 > set Intepreter > New Virtualenv Environment

Terminal > 
 > python -m pip install -r requirements-test.txt
 > python -m pip install -r src/requirements.txt

extract three .pgp files

run a Run/Debug Configuration 
 > setting Python Intepreter to Virtualenv Environment
```
