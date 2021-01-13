# Publishing the App

## 1. Produce .aab

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

---

## 2. Monitoring the release

* [Google Play console](https://play.google.com/apps/publish)

* [Microsoft App Center](https://appcenter.ms/users/jameshnsears/apps/QuoteUnquote)

* [Google Cloud Platform](https://console.cloud.google.com/home/dashboard)

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

## 7. Visual Studio Code Extensions

``` text
$ code --list-extensions
DavidAnson.vscode-markdownlint
GitHub.vscode-pull-request-github
ms-azuretools.vscode-docker
ms-kubernetes-tools.vscode-kubernetes-tools
ms-python.python
ms-vscode-remote.remote-containers
ms-vscode.hexeditor
ms-vsliveshare.vsliveshare
ms-vsliveshare.vsliveshare-audio
ms-vsliveshare.vsliveshare-pack
msjsdiag.debugger-for-chrome
redhat.fabric8-analytics
redhat.vscode-xml
redhat.vscode-yaml
SonarSource.sonarlint-vscode
vscodevim.vim
```

---

## 8. Android Studio 4.1.1 Plugins

* Checkstyle-IDE - Jamie Shiell
* Markdown - JetBrains
* SonarLint - SonarfSource

---


npm install -g appcenter-cli

appcenter login
# popup browser window, that gives APPCENTER_ACCESS_TOKEN token:
# https://appcenter.ms/cli-login?hostname=jsears-Latitude-E6330

export APPCENTER_ACCESS_TOKEN=XXX

./gradlew clean assembleGoogleplay

export APP_GOOGLEPLAY_DEBUG=app/build/outputs/apk/googleplay/debug/app-googleplay-debug.apk
appcenter distribute release --app "jameshnsears/QuoteUnquote-Development" --file ${APP_GOOGLEPLAY_DEBUG} --group Collaborators --release-notes "GitHub CI" --token $APPCENTER_ACCESS_TOKEN --silent

export APP_GOOGLEPLAY_RELEASE_UNSIGNED=app/build/outputs/apk/googleplay/release/app-googleplay-release-unsigned.apk
appcenter distribute release --app "jameshnsears/QuoteUnquote-Production" --file ${APP_GOOGLEPLAY_RELEASE_UNSIGNED} --group Collaborators --release-notes "GitHub CI" --token $APPCENTER_ACCESS_TOKEN --silent


./gradlew assembleFdroid

export APP_FDROID_DEBUG=build/outputs/apk/fdroid/debug/app-fdroid-debug.apk
appcenter distribute release --app "jameshnsears/QuoteUnquote-Development" --file ${APP_FDROID_DEBUG} --group Collaborators --release-notes "GitHub CI" --token $APPCENTER_ACCESS_TOKEN --silent

export APP_FDROID_RELEASE_UNSIGNED=build/outputs/apk/fdroid/release/app-fdroid-release-unsigned.apk
appcenter distribute release --app "jameshnsears/QuoteUnquote-Development" --file ${APP_FDROID_RELEASE_UNSIGNED} --group Collaborators --release-notes "GitHub CI" --token $APPCENTER_ACCESS_TOKEN --silent

