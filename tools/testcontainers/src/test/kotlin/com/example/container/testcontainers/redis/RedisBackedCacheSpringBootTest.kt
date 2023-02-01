package com.example.container.testcontainers.redis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.Jedis

@SpringBootTest
@ContextConfiguration(initializers = [RedisBackedCacheSpringBootTest.Initializer::class])
class RedisBackedCacheSpringBootTest {

  companion object {
    val redisContainer = GenericContainer<Nothing>("redis:3-alpine")
      .apply { withExposedPorts(6379) }
  }

  internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
      redisContainer.start()

      TestPropertyValues.of(
        "spring.redis.host=${redisContainer.host}",
        "spring.redis.port=${redisContainer.firstMappedPort}"
      ).applyTo(configurableApplicationContext.environment)
    }
  }

  private lateinit var cache: Cache


  @BeforeEach
  fun setUp() {
    val jedis = Jedis(redisContainer.getHost(), redisContainer.getMappedPort(6379))
    cache = RedisBackedCache(jedis, "test")
    println(cache)
  }

  @Test
  fun testFindingAnInsertedValue() {
    cache.put("foo", "FOO")
    val foundObject: String? = cache.get("foo", String::class.java)
    assertThat(foundObject)
      .isNotNull()
      .isEqualTo("FOO")
  }

  @Test
  fun testNotFindingAValueThatWasNotInserted() {
    val foundObject: String? = cache.get("bar", String::class.java)
    assertThat(foundObject)
      .isNull()
  }
}