# publish coverage to covecov.io - requires device to be connected, but much faster & more reliable than external CI

# NOTE: branch needs to have been pushed to GitHub

export GIT_HEAD_HASH=`git rev-parse HEAD`
export GIT_BRANCH=`git rev-parse --abbrev-ref HEAD`
export CODECOV_UPLOADER_NAME=`hostname`

./gradlew clean :app:testGoogleplayDebugCoverage --stacktrace
bash <(curl https://codecov.io/bash) -v -t ${CODECOVIO_TOKEN} -C ${GIT_HEAD_HASH} -B ${GIT_BRANCH} -n ${CODECOV_UPLOADER_NAME} -f app/build/reports/GoogleplayDebug.xml -F app.gooleplay

./gradlew clean :app:testManifestTestDebugCoverage --stacktrace
bash <(curl https://codecov.io/bash) -v -t ${CODECOVIO_TOKEN} -C ${GIT_HEAD_HASH} -B ${GIT_BRANCH} -n ${CODECOV_UPLOADER_NAME} -f app/build/reports/ManifestTestDebug.xml -F app.manifestTest

./gradlew clean :app:testFdroidDebugCoverage --stacktrace
bash <(curl https://codecov.io/bash) -v -t ${CODECOVIO_TOKEN} -C ${GIT_HEAD_HASH} -B ${GIT_BRANCH} -n ${CODECOV_UPLOADER_NAME} -f app/build/reports/FdroidDebug.xml -F app.froid
