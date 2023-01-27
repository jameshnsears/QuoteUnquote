# Checklist - Release

[ ] API level 24 emulator: get latest /sdcard/Download/Quote_Unquote_ori_portrait.png for featureGraphic.png
[ ] API level 33, Pixel 6: get latest screenshots 
[ ] test application install on top of prior version, see step 5. in NOTES.md
[ ] delete development and extract production app/google-services.json
[ ] update versionCode + versionName in app/build.gradle
[ ] make sure changelogs/xxx.txt matches versionCode
[ ] update app/schemas/com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase/xx.json to match AbstractQuotationDatabase.java
[ ] examine merged manifest, especially for sdk version and permissions
[ ] deploy app-googleplay-release.aab
[ ] tag the release
[ ] delete production and extract development google-services.json
[ ] store a -fdroid release on GitHub releases
