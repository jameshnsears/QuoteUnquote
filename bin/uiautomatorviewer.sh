#!/bin/bash

# https://www.openlogic.com/openjdk-downloads?field_java_parent_version_target_id=416&field_operating_system_target_id=426&field_architecture_target_id=391&field_java_package_target_id=396

ANDROID_SDK_PATH=$HOME/Android/Sdk
JAVA_HOME=$HOME/bin/openlogic-openjdk-8u342-b07-linux-x64/jre
PATH=$JAVA_HOME/bin:$PATH

# openjdk version "1.8.0_342-342"
# OpenJDK Runtime Environment (build 1.8.0_342-342-b07)
# OpenJDK 64-Bit Server VM (build 25.342-b07, mixed mode)
# java -version

$HOME/Android/Sdk/tools/bin/uiautomatorviewer

java -Xmx1600M -Dcom.android.uiautomator.bindir="ANDROID_SDK_PATH/tools" -cp "ANDROID_SDK_PATH/tools/lib/x86_64/swt.jar":"ANDROID_SDK_PATH/tools/lib/*" com.android.uiautomator.UiAutomatorViewer
