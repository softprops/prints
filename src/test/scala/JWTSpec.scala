package prints

import org.scalatest.FunSpec

class JWTSpec extends FunSpec {
  describe("JWT") {
    it ("should sign stuff") {
      assert(JWT(Header("none"), Claims("foo" -> "bar"), "test".getBytes("utf8")).map(new String(_))
             === Some("eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0=.eyJmb28iOiJiYXIifQ==."))
    }
  }
}
