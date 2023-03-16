#!/bin/bash

source envvars.sh

if [ $# -ne 2 ]; then
  echo "Usage: $0 <BRANCH> <NUMBER_OF_RUNS>"
  exit 1
fi

BRANCH=$1
NUMBER_OF_RUNS=$2

gh run list -b "${BRANCH}" --json databaseId  -q '.[].databaseId' -L ${NUMBER_OF_RUNS} |
  xargs -IID gh api \
    "repos/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions/runs/ID" \
    -X DELETE
