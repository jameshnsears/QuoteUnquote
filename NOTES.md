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
* checkout prio version & build debug with prod db
* adb install -r "app/build/outputs/apk/googleplay/release/app-googleplay-release.apk"
* look at Logcat
* checkout HEAD, and run debug with prod db
