#!/bin/sh
set -e

./gradlew -q \
  checkstyle \
  detekt \
  ktlint \
  lintFdroidDebug \
  lintGoogleplayDebug
