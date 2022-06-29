#!/bin/bash

# publish combined coverage to covecov.io; more relioble then Github workflow runner
# - requires emulator at API 30 to be connected (for all tests to be run)
# - branch needs to have been pushed to GitHub for codecov.io to show src
# - run from project root folder / runtime profile

source bin/envvars.sh
echo ${CODECOVIO_TOKEN}

GIT_HEAD_HASH=$(git rev-parse HEAD)
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
CODECOV_UPLOADER_NAME=$(hostname)

./gradlew clean uninstallAll :app:testGoogleplayDebugCombinedCoverage --stacktrace
bash <(curl https://codecov.io/bash) -t "${CODECOVIO_TOKEN}" -C "${GIT_HEAD_HASH}" -b 0 -B "${GIT_BRANCH}" -n "${CODECOV_UPLOADER_NAME}" -f "app/build/reports/GoogleplayDebug.xml" -F app.gooleplay

./gradlew clean uninstallAll :app:testFdroidDebugCombinedCoverage --stacktrace
bash <(curl https://codecov.io/bash) -t "${CODECOVIO_TOKEN}" -C "${GIT_HEAD_HASH}" -b 0 -B "${GIT_BRANCH}" -n "${CODECOV_UPLOADER_NAME}" -f "app/build/reports/FdroidDebug.xml" -F app.froid
