# Checklist - Release

## GPG

```text
[ ] ensure IDE using signed commits
[ ] ensure all .gpg files are extracted

find . -name "*.gpg"
./app/google-services.json-development.gpg
./app/keystore.readme.gpg
./app/keystore.jks.gpg
./app/google-services.json-production.gpg
./local.properties.gpg
./bin/envvars.sh.gpg
./QuoteUnquote.cloudLib/cloudLib/local.properties.gpg
./QuoteUnquote.cloudLib.functions/local.properties.gpg
./QuoteUnquote.cloudLib.functions/bin/tidy-up-github-actions.sh.gpg
./QuoteUnquote.cloudLib.functions/system_test_dev.sh.gpg
./QuoteUnquote.cloudLib.functions/config/prod-service-account.json.gpg
./QuoteUnquote.cloudLib.functions/config/dev-service-account.json.gpg
./QuoteUnquote.utilsLib/utilsLib/local.properties.gpg
```

## Assets

```text
[ ] Medium Phone API 36.0 AOSP emulator, Medium Phone, get latest screenshots 
```

## General

```text
[ ] Build > Rebuild project - to avoid Gradle using cached files
[ ] test application install on top of prior version, see step 5. in NOTES.md
[ ] align assets
[ ] delete development and extract production app/google-services.json
[ ] update versionCode + versionName in app/build.gradle
[ ] make sure changelogs/xxx.txt matches versionCode
[ ] update app/schemas/com.github.jameshnsears.quoteunquote.database.quotation.QuotationDatabase/xx.json to match AbstractQuotationDatabase.java
[ ] deploy app-googleplay-release.aab
[ ] deploy app/build/outputs/mapping/googleplayRelease/mapping.txt
[ ] tag the release, and push tag
[ ] delete production and extract development google-services.json
```
