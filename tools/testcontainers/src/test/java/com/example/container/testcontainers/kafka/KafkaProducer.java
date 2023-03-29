package com.example.container.testcontainers.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("dummy")
public class KafkaProducer {

  @Autowired
  private KafkaTemplate<String, String> kafkaTemplate;

  public void send(String topic, String payload) {
    log.info("sending payload='{}' to topic='{}'", payload, topic);
    kafkaTemplate.send(topic, payload);
  }
}