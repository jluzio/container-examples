package com.example.container.testcontainers.redis

import com.google.gson.Gson
import redis.clients.jedis.Jedis
import java.util.*

/**
 * An implementation of [Cache] that stores data in Redis.
 */
class RedisBackedCache(private val jedis: Jedis, private val cacheName: String) : Cache {

  private val gson: Gson = Gson()

  override fun put(key: String, value: Any?) {
    val jsonValue = gson.toJson(value)
    jedis.hset(cacheName, key, jsonValue)
  }

  override fun <T> get(key: String, expectedClass: Class<T>): T? {
    val foundJson = jedis.hget(cacheName, key) ?: return null
    return gson.fromJson(foundJson, expectedClass)
  }
}