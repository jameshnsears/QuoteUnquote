#!/bin/bash

# publish coverage to covecov.io - requires device to be connected, but much faster & more reliable than external CI
#
# NOTE:
#   - branch needs to have been pushed to GitHub for codecov.io to show src
#   - run from project root folder

GIT_HEAD_HASH=$(git rev-parse HEAD)
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
CODECOV_UPLOADER_NAME=$(hostname)

./gradlew clean :app:testGoogleplayDebugCombinedCoverage --stacktrace
bash <(curl https://codecov.io/bash) -t "${CODECOVIO_TOKEN}" -C "${GIT_HEAD_HASH}" -b 0 -B "${GIT_BRANCH}" -n "${CODECOV_UPLOADER_NAME}" -f "app/build/reports/GoogleplayDebug.xml" -F app.gooleplay

./gradlew clean :app:testFdroidDebugCombinedCoverage --stacktrace
bash <(curl https://codecov.io/bash) -t "${CODECOVIO_TOKEN}" -C "${GIT_HEAD_HASH}" -b 0 -B "${GIT_BRANCH}" -n "${CODECOV_UPLOADER_NAME}" -f "app/build/reports/FdroidDebug.xml" -F app.froid
