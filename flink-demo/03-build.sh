#!/bin/sh

set -e
cd "$(dirname -- "$0")"

(cd ..; ./gradlew flink_demo_application:build flink_demo_application:shadowJar)
(cd application; cp ./build/libs/flink_demo_application-0.1.0-all.jar ../../flink/jars)

