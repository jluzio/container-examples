package com.example.container.testcontainers.redis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RedisController {

  private val key = "testcontainers"

  @Autowired
  private lateinit var redisTemplate: RedisTemplate<String, String>

  @PostMapping("/set-foo")
  fun setFoo(@RequestBody value: String) {
    redisTemplate.opsForValue().set(key, value)
  }

  @GetMapping("/get-foo")
  fun getFoo(): String? {
    return redisTemplate.opsForValue().get(key)
  }
}