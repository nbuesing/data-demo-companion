#!/bin/bash

set -e

cd "$(dirname -- "$0")/.."

(cd flink; docker compose down -v)
(cd kafka-1; docker compose down -v)
