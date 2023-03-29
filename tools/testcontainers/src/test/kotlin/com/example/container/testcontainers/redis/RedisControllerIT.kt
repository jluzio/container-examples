package com.example.container.testcontainers.redis

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.testcontainers.containers.GenericContainer

@SpringBootTest
@ContextConfiguration(initializers = [RedisControllerIT.Initializer::class])
@AutoConfigureMockMvc
class RedisControllerIT {

  companion object {
    val redisContainer = GenericContainer<Nothing>("redis:7-alpine")
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

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Test
  fun testRedisFunctionality() {
    val greeting = "Hello Testcontainers with Kotlin"
    mockMvc.perform(post("/set-foo").content(greeting))
      .andExpect(status().isOk)

    mockMvc.perform(get("/get-foo"))
      .andExpect(status().isOk)
      .andExpect(content().string(containsString(greeting)))
  }
}