#!/bin/bash

source bin/envvars.sh

if [ $# -ne 2 ]; then
  echo "Usage: $0 <TAG>"
  exit 1
fi

TAG=$1

######################################
echo "build..."
./gradlew clean assembleGoogleplayRelease --stacktrace

######################################
echo "sign..."
apksigner sign --ks app/keystore.jks --ks-key-alias $KEYSTORE_ALIAS --ks-pass env:KEYSTORE_PASSWORD --out ./app-googleplay-release-signed.apk ./app/build/outputs/apk/googleplay/release/app-googleplay-release.apk

######################################
echo "note..."
bin/release-note.sh

######################################
echo "publish..."
gh repo set-default jameshnsears/QuoteUnquote
gh release create $TAG --latest -F bin/release-github-notes.txt ./app-googleplay-release-signed.apk
