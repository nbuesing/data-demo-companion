package io.kineticedge.flink;


import io.kineticedge.flink.avro.AvroDeserialization;
import io.kineticedge.flink.avro.AvroSerialization;
import io.kineticedge.order.Order;
import io.kineticedge.order.OrderEnriched;
import io.kineticedge.order.OrderKey;
import io.kineticedge.order.OrderLineItem;
import io.kineticedge.purchaseorder.PurchaseOrder;
import io.kineticedge.purchaseorder.PurchaseOrderLineItem;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.time.Duration;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

public class KafkaRead {

  private static final Random RANDOM = new Random();

  public static void main(String[] args) throws Exception {

    Configuration configuration = new Configuration();
    configuration.setString("pipeline.name", "example-pipeline");

    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment()
            .enableCheckpointing(5000L);

    env.configure(configuration);

    String bootStrapServers = "broker-1:9092,broker-2:9092,broker-3:9092";

    Properties kafkaProperties = new Properties();
    kafkaProperties.setProperty("bootstrap.servers", bootStrapServers);

    // streamsBuilder.source()
    KafkaSource<Tuple2<OrderKey, Order>> source = KafkaSource.<Tuple2<OrderKey, Order>>builder()
            .setBootstrapServers(bootStrapServers)
            .setTopics("datagen.orders")
            .setDeserializer(new AvroDeserialization<>(OrderKey.class, Order.class, "http://schema-registry:8081"))
            .setProperty("commit.offsets.on.checkpoint", "true")
            .setProperty("group.id", "flink-example")
            .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
            .build();

//    // streamsbuilder.to()
//    KafkaSink<Tuple2<OrderKey, PurchaseOrder>> sink = KafkaSink.<Tuple2<OrderKey, PurchaseOrder>>builder()
//            .setBootstrapServers(bootStrapServers)
//            .setRecordSerializer(new AvroSerialization<>(OrderKey.class, PurchaseOrder.class, "purchase-orders", "http://schema-registry:8081"))
//            .build();
//
//    // topology
//    env
//            .fromSource(source, WatermarkStrategy.noWatermarks(), "DatagenOrders")
//            .map(kv -> {
//              return new Tuple2<>(kv.f0, convert(kv.f1));
//            }, new TupleTypeInfo<>(TypeInformation.of(OrderKey.class), TypeInformation.of(PurchaseOrder.class)))
//            .sinkTo(sink);



    // HandsOn

    KafkaSource<Tuple2<OrderKey, OrderEnriched>> source2 = KafkaSource.<Tuple2<OrderKey, OrderEnriched>>builder()
                    .setBootstrapServers(bootStrapServers)
                    .setTopics("datagen.orders.enriched")
                    .setDeserializer(new AvroDeserialization<>(OrderKey.class, OrderEnriched.class, "http://schema-registry:8081"))
                    .setProperty("commit.offsets.on.checkpoint", "true")
                    .setProperty("group.id", "flink-example")
                    .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST))
                    .build();

    KafkaSink<Tuple2<OrderKey, PurchaseOrder>> sink2 = KafkaSink.<Tuple2<OrderKey, PurchaseOrder>>builder()
            .setBootstrapServers(bootStrapServers)
            .setRecordSerializer(new AvroSerialization<>(OrderKey.class, PurchaseOrder.class, "purchase-orders-2", "http://schema-registry:8081"))
            .build();

    DataStream<Tuple2<OrderKey, Order>> orderStream =
            env.fromSource(source, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5)), "DatagenOrders-1");

//    DataStream<Tuple2<OrderKey, OrderEnriched>> enrichStream =
//            env.fromSource(source2, WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5)), "DatagenOrders-2");

    orderStream
            .map(order -> new Tuple2<OrderKey, PurchaseOrder>(order.f0, convert(order.f1)))
            .sinkTo(sink2);

//    orderStream.join(enrichStream)
//        .where(tuple -> tuple.f0)
//        .equalTo(tuple -> tuple.f0)
//        .window(TumblingEventTimeWindows.of(Time.seconds(30)))
//        .apply(new JoinFunction<Tuple2<OrderKey, Order>, Tuple2<OrderKey, OrderEnriched>, Tuple2<OrderKey, PurchaseOrder>>() {
//            @Override
//            public Tuple2<OrderKey, PurchaseOrder> join(Tuple2<OrderKey, Order> first, Tuple2<OrderKey, OrderEnriched> second) {
//                PurchaseOrder po = convert(first.f1, second.f1.getEnriched());
//                return new Tuple2<>(first.f0, po);
//            }
//        }).sinkTo(sink2);

    env.execute();

  }

  private static PurchaseOrder convert(Order order) {

    return PurchaseOrder.newBuilder()
            .setOrderId(order.getOrderId())
            .setUserId(order.getUserId())
            .setStoreId(order.getStoreId())
            .setLineItems(order.getLineItems().stream().map(KafkaRead::convert).collect(Collectors.toList()))
            .build();
  }

  private static PurchaseOrder convert(Order order, String extra) {

    return PurchaseOrder.newBuilder()
            .setOrderId(order.getOrderId())
            .setUserId(order.getUserId() + "_" + extra)
            .setStoreId(order.getStoreId())
            .setLineItems(order.getLineItems().stream().map(KafkaRead::convert).collect(Collectors.toList()))
            .build();
  }

  private static PurchaseOrderLineItem convert(OrderLineItem lineItem) {

    final double price = randomPrice();

    return PurchaseOrderLineItem.newBuilder()
            .setSku(lineItem.getSku())
            .setQuantity(lineItem.getQuantity())
            .setPrice(price)
            .setRetailPrice(Math.floor(price * 1.10 * 100) / 100.0)
            .build();
  }

  private static double randomPrice() {
    return ((double) RANDOM.nextInt(10_000)) / 100.0;
  }
}