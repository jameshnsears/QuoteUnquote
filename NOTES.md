## 1. Publishing the App.

### 1.1. Produce .aab
* increment version:
    * app/build.grade
        * versionCode n         <- play console, increment per release
        * versionName "1.0.0"   <- appears to users
    * README.md
* Android Studio > Build > Generate Signed Bundle / APK... > Android App Bundle 
    * using 'keystore.jks' file (after decrypted keystore)
    * key alias: upload
    * Build Variant: release
    * Signature Versions: V1 & V2
* app/release/app-release.aab

### 1.2. Upload .aab
* [Google Play console](https://play.google.com/apps/publish)
    * Release management > App releases > Internal test track 
        * Manage > Create Release
            * Upload .aab
* Fill out all grey round tick boxes - i.e. Content Rating; App Content; Pricing & distribution
* [Privacy Policy](https://jameshnsears.github.io/quoteunquote/privacy_policy.html)

### 1.3. Amazon Appstore
* [Amazon Appstore](https://developer.amazon.com/apps-and-games)

---

## 2. Monitoring the release
* [Google Play console](https://play.google.com/apps/publish)

* [Microsoft App Center](https://appcenter.ms/users/jameshnsears/apps/QuoteUnquote)

* [Google Cloud Platform](https://console.cloud.google.com/home/dashboard)

### 2.1. QA
* [GitHub Actions](https://github.com/jameshnsears/QuoteUnquote/actions)

* [Google Cloud](https://console.cloud.google.com/)

* [Sonar Cloud](https://sonarcloud.io/dashboard?id=jameshnsears_QuoteUnquote)

* [Coveralls](https://coveralls.io/github/jameshnsears/QuoteUnquote)

* [Codecov](https://codecov.io/gh/jameshnsears/QuoteUnquote)

### 2.2. IDE
* https://plugins.jetbrains.com/plugin/7973-sonarlint

---

## 3. Key management

### 3.1. Keystore - keystore.jks
* keytool -genkeypair -alias upload -keyalg RSA -keysize 2048 -validity 9125 -keystore keystore.jks

### 3.2. Public - upload_certificate.pem
* keytool -export -rfc -alias upload -file upload_certificate.pem -keystore keystore.jks

---

## 4. Widget Image Preview
* On the emulator, grant Storage Permission to the Widget Preview app in the app settings.
* Image to be found in: /sdcard/Download

---

## 5. USB Android Debug Bridge
* adb uninstall com.github.jameshnsears.quoteunquote

---

## 6. Device Dependent Issues

### 6.1. MIUI 11 
* 2020-2118/? W/BroadcastQueue: Background execution not allowed: receiving Intent { act=android.intent.action.USER_PRESENT flg=0x24200010 } to com.github.jameshnsears.quoteunquote/.QuoteUnquoteWidget

---

## 7. ui-automator
* sudo apt install openjdk-8-jdk openjdk-8-jre
* export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
* export JRE_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre
* sudo update-alternatives --config java
* /home/$USER/Android/Sdk/tools/bin/uiautomatorviewer

* https://github.com/android/testing-samples/tree/master/ui/uiautomator/BasicSample
* https://developer.android.com/training/testing/ui-automator
* https://developer.android.com/training/testing/ui-testing/uiautomator-testing
