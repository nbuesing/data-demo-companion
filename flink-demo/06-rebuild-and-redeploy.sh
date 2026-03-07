
#!/bin/sh

set -e
cd "$(dirname -- "$0")"

(cd ..; ./gradlew flink_demo_application:build flink_demo_application:shadowJar)
(cd application; cp ./build/libs/flink_demo_application-0.1.0-all.jar ../../flink/jars)

echo ""
echo "jar rebuilt"
sleep 2

#docker exec -it flink_jobmanager sh -c 'flink list | awk "/RUNNING/ {print \$4}" | xargs -r flink cancel && flink run -p 4 --detached /jars/flink_demo_application-0.1.0-all.jar'

docker exec -it flink_jobmanager sh -c '
flink list -a | awk "/(RUNNING|RESTARTING|CREATED|RECONCILING)/ {print \$4}" | xargs -r -n 1 flink cancel
sleep 3
flink list -a
'

echo ""
echo "job canceled"
sleep 2
 
docker exec -it flink_jobmanager sh -c "flink run -p 4 --detached /jars/flink_demo_application-0.1.0-all.jar"

echo ""
echo "setup completed."
