package com.example.container.testcontainers.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class RedisSpringDataTest {

  public record SampleDataRecord(String id, String value) {

  }

  @Data
  @RequiredArgsConstructor
  @AllArgsConstructor
  public static class SampleDataPojo {

    String id;
    String value;
  }

  @Configuration
  @Import({JacksonAutoConfiguration.class})
  static class Config {


    @Bean
    JedisConnectionFactory redisConnectionFactory() {
      // defaults to localhost:6379
      RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(
          redisContainer.getHost(),
          redisContainer.getMappedPort(6379));

      var defaultTimeout = Duration.ofSeconds(5);
      JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
          .readTimeout(defaultTimeout)
          .connectTimeout(defaultTimeout)
          .build();
      return new JedisConnectionFactory(config, clientConfig);
    }

    @Bean
    StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
      return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    RedisTemplate<String, SampleDataRecord> sampleDataRecordRedisTemplate(
        RedisConnectionFactory redisConnectionFactory) {

      Jackson2JsonRedisSerializer<SampleDataRecord> jackson2JsonRedisSerializer =
          new Jackson2JsonRedisSerializer<>(SampleDataRecord.class);

      StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

      RedisTemplate<String, SampleDataRecord> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(redisConnectionFactory);
      redisTemplate.setKeySerializer(stringRedisSerializer);
      redisTemplate.setHashKeySerializer(stringRedisSerializer);
      redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
      redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
      redisTemplate.afterPropertiesSet();
      return redisTemplate;
    }

    @Bean
    RedisTemplate<String, Object> genericRedisTemplate(
        RedisConnectionFactory redisConnectionFactory) {

      // Does not work with records
      ObjectMapper mapper = new ObjectMapper();
      mapper.setVisibility(
          PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
      mapper.activateDefaultTyping(
          mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
      Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer =
          new Jackson2JsonRedisSerializer<>(mapper, Object.class);

      StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

      RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(redisConnectionFactory);
      redisTemplate.setKeySerializer(stringRedisSerializer);
      redisTemplate.setHashKeySerializer(stringRedisSerializer);
      redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
      redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
      redisTemplate.afterPropertiesSet();
      return redisTemplate;
    }
  }

  @Container
  private static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
      .withExposedPorts(6379);

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private RedisTemplate<String, String> stringRedisTemplate;
  @Autowired
  private RedisTemplate<String, SampleDataRecord> sampleDataRecordRedisTemplate;
  @Autowired
  private RedisTemplate<String, Object> genericRedisTemplate;

  @Test
  void string_value() {
    String key = "test";
    var ops = stringRedisTemplate.opsForHash();

    ops.put(key, "foo", "FOO");
    assertThat(ops.get(key, "foo"))
        .isEqualTo("FOO");
  }

  @Test
  void sample_data_value() {
    String key = "test";
    var ops = sampleDataRecordRedisTemplate.opsForHash();

    SampleDataRecord sampleDataRecord = new SampleDataRecord("1", "data1");
    ops.put(key, "sampleData", sampleDataRecord);
    assertThat(ops.get(key, "sampleData"))
        .isEqualTo(sampleDataRecord);
  }

  @Test
  void generic_value() {
    String key = "test";
    var genericOps = genericRedisTemplate.opsForHash();
    var sampleDataRecordOps = sampleDataRecordRedisTemplate.opsForHash();

    SampleDataPojo sampleDataPojo = new SampleDataPojo("1", "data1");
    genericOps.put(key, "sampleDataPojo", sampleDataPojo);
    assertThat(genericOps.get(key, "sampleDataPojo"))
        .isEqualTo(sampleDataPojo);

    SampleDataRecord sampleDataRecord = new SampleDataRecord("1", "data1");
    genericOps.put(key, "sampleDataRecord", sampleDataRecord);
    assertThatThrownBy(() -> genericOps.get(key, "sampleDataRecord"))
        .isInstanceOf(SerializationException.class);

    sampleDataRecordOps.put(key, "sampleDataRecord2", sampleDataRecord);
    assertThat(sampleDataRecordOps.get(key, "sampleDataRecord2"))
        .isEqualTo(sampleDataRecord);
  }
}
