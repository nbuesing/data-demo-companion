#!/bin/sh

set -e

cd "$(dirname -- "$0")"

echo "setup started."
echo ""

echo "installing avro specification to connect cluster for datagen"
echo ""

mkdir -p ../kafka-1/connect-data/datagen
cp -r ./application/src/main/avro/order.avsc ../kafka-1/connect-data/datagen
cp -r ./application/src/main/avro/order-enriched.avsc ../kafka-1/connect-data/datagen

echo "creating topics"
echo ""
#kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic datagen.orders
#kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic purchase-orders

docker exec -it ddc_kafka1_broker-1 sh -c "kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic datagen.orders"
docker exec -it ddc_kafka1_broker-1 sh -c "kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic datagen.orders.enriched"
docker exec -it ddc_kafka1_broker-1 sh -c "kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic purchase-orders"
docker exec -it ddc_kafka1_broker-1 sh -c "kafka-topics --bootstrap-server localhost:9092 --create --if-not-exists --partitions 4 --topic purchase-orders-2"

echo "creating connector 'datagen-orders'"
echo ""
./connect.sh create ./connectors/datagen-orders.json
./connect.sh create ./connectors/datagen-orders-enriched.json

echo "launch application"
echo ""
(cd ..; ./gradlew flink_demo_application:build flink_demo_application:shadowJar)
(cd application; cp ./build/libs/flink_demo_application-0.1.0-all.jar ../../flink/jars)

# for some reason running the below command will sometimes not have the jar available
sleep 1

docker exec -it flink_jobmanager sh -c "flink run -p 4 --detached /jars/flink_demo_application-0.1.0-all.jar"

echo ""
echo "setup completed."
