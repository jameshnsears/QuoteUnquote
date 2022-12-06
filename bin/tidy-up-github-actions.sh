#!/bin/bash

source envvars.sh

# https://blog.oddbit.com/post/2022-09-22-delete-workflow-runs/

# gh run list -b 4.2.0-exclusions -L 2

gh run list --json databaseId  -q '.[].databaseId' -L 100 |
  xargs -IID gh api \
    "repos/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions/runs/ID" \
    -X DELETE