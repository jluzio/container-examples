package com.example.container.testcontainers.redis

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import redis.clients.jedis.Jedis

class RedisBackedCacheTest {

  companion object {

    val redisContainer = GenericContainer<Nothing>("redis:3-alpine")
      .apply {
        withExposedPorts(6379)
        start()
      }
  }

  private lateinit var cache: Cache

  @BeforeEach
  fun setUp() {
    val jedis = Jedis(redisContainer.host, redisContainer.getMappedPort(6379))
    cache = RedisBackedCache(jedis, "test")
    println(cache)
  }

  @Test
  fun testFindingAnInsertedValue() {
    cache.put("foo", "FOO")
    val foundObject: String? = cache.get("foo", String::class.java)
    assertThat(foundObject)
      .isNotNull
      .isEqualTo("FOO")
  }

  @Test
  fun testNotFindingAValueThatWasNotInserted() {
    val foundObject: String? = cache.get("bar", String::class.java)
    assertThat(foundObject)
      .isNull()
  }
}