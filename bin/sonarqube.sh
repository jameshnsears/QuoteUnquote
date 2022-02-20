#!/bin/bash

# https://docs.sonarqube.org/latest/setup/get-started-2-minutes/
# docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest
# http://localhost:9000
# admin/admin
# login; create local project; generate token - i.e. 6aa4d823149ca9566bb0c3b533d0c6cc80fdb27a

# docker network inspect bridge | grep IPv4Address

./gradlew sonarqube \
  -Dsonar.projectKey=QuoteUnquote \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=6aa4d823149ca9566bb0c3b533d0c6cc80fdb27a
