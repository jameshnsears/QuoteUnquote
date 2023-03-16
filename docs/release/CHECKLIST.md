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
[ ] API level 24 emulator: get latest /sdcard/Download/Quote_Unquote_ori_portrait.png for featureGraphic.png
[ ] API level 33, Pixel 6: get latest screenshots 
```

## General
```text
[ ] test application install on top of prior version, see step 5. in NOTES.md
[ ] delete development and extract production app/google-services.json
[ ] update versionCode + versionName in app/build.gradle
[ ] make sure changelogs/xxx.txt matches versionCode
[ ] update app/schemas/com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase/xx.json to match AbstractQuotationDatabase.java
[ ] examine merged manifest, especially for sdk version and permissions
[ ] deploy app-googleplay-release.aab
[ ] deploy app/build/outputs/mapping/googleplayRelease/mapping.txt
[ ] tag the release, and push tags
[ ] delete production and extract development google-services.json
[ ] store a -fdroid release on GitHub releases
```
