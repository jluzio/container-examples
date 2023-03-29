package javatest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JavaFooTest {

  @Test
  void test() {
    assertThat(new JavaFoo().run())
        .isEqualTo("java-foo");
  }

}
