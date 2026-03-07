
#!/bin/sh

set -e
cd "$(dirname -- "$0")"

(cd ..; ./gradlew flink_demo_application:build flink_demo_application:shadowJar)
(cd application; cp ./build/libs/flink_demo_application-0.1.0-all.jar ../../flink/jars)

sleep 1

docker exec -it flink_jobmanager sh -c "flink run -p 4 --detached /jars/flink_demo_application-0.1.0-all.jar"

echo ""
echo "setup completed."
