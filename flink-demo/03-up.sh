#!/bin/bash

set -e

#
# make sure the current directory is the 'data-demo-companion' directory
# 

cd "$(dirname -- "$0")/.."

#
# allow both 'kafka-1' and 'flink' to start concurrently
#

(cd kafka-1; docker compose up -d)
(cd flink; docker compose up -d)

#
# how wait on them by starting them again, so script doesn't complete until both are up and healthy
#

(cd kafka-1; docker compose up -d --wait)
(cd flink; docker compose up -d --wait)

