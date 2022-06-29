# Checklist - Release

* API level 25 emulator: get latest /sdcard/Download/Quote_Unquote_ori_portrait.png for featureGraphic.png
* test application install on top of prior version, see step 5. in NOTES.md
* delete development and extract production app/google-services.json
* update versionCode + versionName in app/build.gradle
* make sure changelogs/xx.txt matches versionCode
* update app/schemas/com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase/xx.json to match AbstractQuotationDatabase.java
* examine merged manifest, especially for sdk version and permissions
* deploy app-googleplay-release.aab
* tag the release
* delete production and extract development google-services.json
