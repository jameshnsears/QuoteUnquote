# Notes

## 1. Produce .aab - manually

* increment version:
  * app/build.grade
    * versionCode n         <- play console, increment per release
    * versionName "1.0.0"   <- appears to users
* README.md
* Android Studio > Build > Generate Signed Bundle / APK... > Android App Bundle
  * using 'keystore.jks'
  * key alias: upload
  * Build Variant: release
  * Signature Versions: V1 & V2
* app/release/app-release.aab

---

## 2. Key management

### 2.1. Keystore - keystore.jks

* keytool -genkeypair -alias upload -keyalg RSA -keysize 2048 -validity 9125 -keystore keystore.jks

### 2.2. Public - upload_certificate.pem

* keytool -export -rfc -alias upload -file upload_certificate.pem -keystore keystore.jks

---

## 3. Widget Image Preview

* On the emulator, grant Storage Permission to the Widget Preview app in the app settings.
* Image to be found in: /sdcard/Download

---

## 4. Android Studio Plugins

* Checkstyle-IDE - Jamie Shiell
* SonarLint - SonarSource

---

## 5. Test new .apk over prior .apk via emulator / device

* adb uninstall com.github.jameshnsears.quoteunquote
* checkout prior version
* clean + googleplayDebug + with quotations.db.prod + gradle sync
* Build > Build Bundle(s) / APK(s) > > Build APK(s)
* adb install -r app/build/outputs/apk/googleplay/debug/app-googleplay-debug.apk
* or
* adb install -r 2.1.1-4f4b8b28-app-googleplay-debug.apk
* adb logcat -c
* adb logcat > logcat &
* checkout HEAD, build + install with above & observe after install

---

## 6. proguard mapping file

* /app/build/outputs/mapping/googleplayRelease/mapping.txt

---

## 7. Release Checklist

. delete development and extract production app/google-services.json
. update versionCode + versionName in app/build.gradle
. make sure changelogs/xx.txt matches versionCode
. update app/schemas/com.github.jameshnsears.quoteunquote.database.quotation.AbstractQuotationDatabase/xx.json to match AbstractQuotationDatabase.java
. ensure fastlane images match app - i.e. database count
. ensure <string-array name="preview_quotations"> strings.xml matches database count
. deploy app-googleplay-release.aab
. tag the release
. delete production and extract development google-services.json