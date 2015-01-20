package prints

import org.scalatest.FunSpec

class JWTSpec extends FunSpec {
  describe("JWT") {
    it ("should support unsigned tokens") {
      assert(JWT(Header("none"), Claims("foo" -> "bar"),
                 Algorithm.Key.Bytes("test".getBytes("utf8"))).map(new String(_))
             === Some("eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0=.eyJmb28iOiJiYXIifQ==."))
    }

    it ("should support HS256 signed tokens") {
      assert(JWT(Header("HS256"), Claims("foo" -> "bar"),
               Algorithm.Key.Bytes("test".getBytes("utf8"))).map(new String(_))
             === Some("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.9EuBjGzXHRD27zPobLmSWM6LQ7zyE8O6nvTC0_yB5a0="))
    }

    it ("should support HS384 signed tokens") {
      assert(JWT(Header("HS384"), Claims("foo" -> "bar"),
               Algorithm.Key.Bytes("test".getBytes("utf8"))).map(new String(_))
             === Some("eyJhbGciOiJIUzM4NCIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.OTq6Gb7tJjP5M1eiJr_g9KfSselRtnmVrTmiiF1HZUQ8jvqNYdUO6YHZnDMgeJfe"))
    }

    it ("should support HS512 signed tokens") {
      assert(JWT(Header("HS512"), Claims("foo" -> "bar"),
               Algorithm.Key.Bytes("test".getBytes("utf8"))).map(new String(_))
             === Some("eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIifQ==.j0C9Km2iSDyASM-A74nriUpCh6PqHQUW-4Gx54WfsJLbI0EyMGdMybqF04YlpJb71F9bC4Qrny4cvB_F3fv3Dw=="))
    }

    it ("should verify what it creates") {
      val header = Header("HS256")
      val clients = Map("1" -> Algorithm.Key.Bytes("test".getBytes("utf8")))
      val claims = Claims("aud" -> "1", "foo" -> "bar")
      val verified = for {
        JWT(h, c, s) <- JWT(header, claims, clients("1")).map(new String(_))
        client       <- c.get("aud")
        key          <- clients.get(client)
        (_, vc, _)   <- JWT.verify((h, c, s), key)
      } yield vc
      assert(verified.isDefined)
    }
  }
}
