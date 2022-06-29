#!/bin/bash

# https://docs.sonarqube.org/latest/setup/get-started-2-minutes/
# docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest
# http://localhost:9000
# admin/admin
# login; create local project; generate token - i.e. sqp_29b40c2eba45511c0eb8cc1ad62f19cbd210ffe4

# docker network inspect bridge | grep IPv4Address

./gradlew sonarqube \
  -Dsonar.projectKey=QuoteUnquote \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=sqp_29b40c2eba45511c0eb8cc1ad62f19cbd210ffe4
