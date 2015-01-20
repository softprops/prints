package prints

import org.scalatest.FunSpec

class JWTSpec extends FunSpec {
  describe("JWT") {
    it ("should support unsigned tokens") {
      assert(JWT(Header("none"), Claims("foo" -> "bar"),
                 "test".getBytes("utf8")).map(new String(_))
             === Some("eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0=.eyJmb28iOiJiYXIifQ==."))
    }

    it ("should support HS256 signed tokens") {
      assert(JWT(Header("HS256"), Claims("foo" -> "bar"),
               "test".getBytes("utf8")).map(new String(_))
             === Some("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.9EuBjGzXHRD27zPobLmSWM6LQ7zyE8O6nvTC0_yB5a0="))
    }

    it ("should support HS384 signed tokens") {
      assert(JWT(Header("HS384"), Claims("foo" -> "bar"),
               "test".getBytes("utf8")).map(new String(_))
             === Some("eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.OTq6Gb7tJjP5M1eiJr_g9KfSselRtnmVrTmiiF1HZUQ8jvqNYdUO6YHZnDMgeJfe"))
    }

    it ("should support HS512 signed tokens") {
      assert(JWT(Header("HS512"), Claims("foo" -> "bar"),
               "test".getBytes("utf8")).map(new String(_))
             === Some("eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.j0C9Km2iSDyASM-A74nriUpCh6PqHQUW-4Gx54WfsJLbI0EyMGdMybqF04YlpJb71F9bC4Qrny4cvB_F3fv3Dw=="))
    }
  }
}
