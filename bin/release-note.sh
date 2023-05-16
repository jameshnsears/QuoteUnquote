#!/bin/bash

CHANGELOG_FOLDER=fastlane/metadata/android/en-US/changelogs
LATEST_CHANGELOG_NUMBER=$(ls $CHANGELOG_FOLDER | sed 's/\([0-9]\+\).*/\1/g' | sort -n | tail -1)

RELEASE_NOTE=release-github-notes.md

echo "### Changelog: $LATEST_CHANGELOG_NUMBER" > $RELEASE_NOTE
cat $CHANGELOG_FOLDER/$LATEST_CHANGELOG_NUMBER.txt >> $RELEASE_NOTE
echo ""  >> $RELEASE_NOTE
echo ""  >> $RELEASE_NOTE
echo "### Instructions - for using .apk" >> $RELEASE_NOTE
echo "* **Sync > Backup** your existing data" >> $RELEASE_NOTE
echo "* Uninstall the App" >> $RELEASE_NOTE
echo "* Install .apk supplied here" >> $RELEASE_NOTE

sed -i "s/&bull;/*/g" $RELEASE_NOTE
