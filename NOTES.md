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

## 4. USB Android Debug Bridge

* adb uninstall com.github.jameshnsears.quoteunquote

---

## 5. Device Dependent Issues :-(

### 5.1. MIUI 11

* 2020-2118/? W/BroadcastQueue: Background execution not allowed: receiving Intent { act=android.intent.action.USER_PRESENT flg=0x24200010 } to com.github.jameshnsears.quoteunquote/.QuoteUnquoteWidget

### 5.2. MIUI 12 - ./gradlew <*>CoverageCombined task

```text
2021-01-19 16:12:02.853 22522-22522/? W/installer: type=1400 audit(0.0:17528): avc: denied { search } for name="nativetest" dev="dm-2" ino=14 scontext=u:r:shell:s0 tcontext=u:object_r:nativetest_data_file:s0 tclass=dir permissive=0
2021-01-19 16:12:02.853 22522-22522/? W/installer: type=1400 audit(0.0:17529): avc: denied { search } for name="nativetest64" dev="dm-2" ino=17 scontext=u:r:shell:s0 tcontext=u:object_r:nativetest_data_file:s0 tclass=dir permissive=0
2021-01-19 16:12:02.853 22522-22522/? W/installer: type=1400 audit(0.0:17530): avc: denied { search } for name="nativetest" dev="dm-2" ino=14 scontext=u:r:shell:s0 tcontext=u:object_r:nativetest_data_file:s0 tclass=dir permissive=0
2021-01-19 16:12:03.133 1999-5941/? I/ActivityManager: Force stopping com.github.jameshnsears.quoteunquote appid=10691 user=0: from process:22539
2021-01-19 16:12:03.140 2645-2645/? E/PhoneInterfaceManager: [PhoneIntfMgr] getCarrierPackageNamesForIntent: No UICC
2021-01-19 16:12:03.533 890-890/? W/surfaceflinger: type=1400 audit(0.0:17540): avc: denied { read } for name="u:object_r:vendor_fp_prop:s0" dev="tmpfs" ino=22743 scontext=u:r:surfaceflinger:s0 tcontext=u:object_r:vendor_fp_prop:s0 tclass=file permissive=0
2021-01-19 16:12:03.550 890-890/? E/libc: Access denied finding property "ro.hardware.fp.fod"
2021-01-19 16:12:03.550 890-890/? E/libc: Access denied finding property "ro.hardware.fp.sideCap"
2021-01-19 16:12:03.664 1999-2035/? I/ActivityManager: Force stopping com.github.jameshnsears.quoteunquote.test appid=10692 user=0: from process:22555

```

## 6. Android Studio Plugins

* Checkstyle-IDE - Jamie Shiell
* SonarLint - SonarSource

## 7. Unreliable CI emulator

[![CircleCI](https://circleci.com/gh/jameshnsears/QuoteUnquote.svg?style=svg)](https://github.com/jameshnsears/QuoteUnquote)
