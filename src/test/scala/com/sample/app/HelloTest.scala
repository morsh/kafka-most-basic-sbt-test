package com.sample.app

import org.scalatest._

class HelloTest extends FlatSpec with Matchers {
  "The Hello object" should "say hello" in {
    Hello.greeting shouldEqual "hello"
  }
}
