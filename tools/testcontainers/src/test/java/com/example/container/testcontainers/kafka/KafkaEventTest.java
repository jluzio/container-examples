package com.example.container.testcontainers.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@EmbeddedKafka(topics = "test-topic")
class KafkaEventTest {

  private static KafkaContainer kafkaContainer;
  private static KafkaProducer<String, String> producer;
  private static KafkaConsumer<String, String> consumer;
  private static final String TOPIC_NAME = "test-topic";
  private static final String TEST_MESSAGE = "Hello, world!";

  @BeforeAll
  static void setup() {
    // Start a Kafka container using Testcontainers
    kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    kafkaContainer.start();

    // Set up a Kafka producer
    Map<String, Object> producerConfigs = KafkaTestUtils.producerProps(kafkaContainer.getBootstrapServers());
    producer = new KafkaProducer<>(producerConfigs);

    // Set up a Kafka consumer
    Map<String, Object> consumerConfigs = KafkaTestUtils.consumerProps("test-group", "false", kafkaContainer.getBootstrapServers());
    consumer = new KafkaConsumer<>(consumerConfigs);
    consumer.subscribe(Collections.singleton(TOPIC_NAME));
  }

  @AfterAll
  public static void cleanup() {
    // Stop the Kafka container
    kafkaContainer.stop();
  }

  @Test
  void testSendMessage() throws InterruptedException {
    // Send a test message
    producer.send(new ProducerRecord<>(TOPIC_NAME, TEST_MESSAGE));

    // Wait for the message to be received by the consumer
    BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
    consumer.poll(Duration.ofSeconds(10)).forEach(records::add);

    // Assert that the message was received by the consumer
    ConsumerRecord<String, String> record = records.poll(10, TimeUnit.SECONDS);
    assert record != null;
    assert record.value().equals(TEST_MESSAGE);
  }
}
