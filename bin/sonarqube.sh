#!/bin/bash

# https://docs.sonarqube.org/latest/setup/get-started-2-minutes/

# docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest

# http://localhost:9000
# admin/admin

# docker network inspect bridge | grep IPv4Address

./gradlew clean assembleDebug

docker run \
--rm \
-e SONAR_HOST_URL="http://172.17.0.2:9000" \
-e SONAR_LOGIN=ff738b403467e832bf666b963b677849b2a98b0d \
-v "$HOME/GIT_REPOS/QuoteUnquote:/usr/src" \
sonarsource/sonar-scanner-cli
