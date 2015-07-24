package prints

import java.util.Arrays
import scala.concurrent.duration._

object JWT {
  private[this] val dot = ".".getBytes

  private def encode(bytes: Array[Byte]): Array[Byte] =
    base64.Encode.urlSafe(bytes)

  private def decodeBytes(str: String) =
    base64.Decode.urlSafe(str)

  private def decode(str: String) =
    decodeBytes(str).right.map(new String(_, "utf8"))

  private[this] val NoLeeway = 0.seconds

  /** join two byte arrays separating with a '.' */
  private def join(a: Array[Byte], b: Array[Byte]) = {
    def concat(x: Array[Byte], y: Array[Byte]) = {
      val out = new Array[Byte](x.length + y.length)
      System.arraycopy(x, 0, out, 0, x.length)
      System.arraycopy(y, 0, out, x.length, y.length)
      out
    }
    concat(concat(a, dot), b)
  }

  def apply(
    header: Header,
    claims: Claims,
    key: Algorithm.Key): Option[Array[Byte]] = {
    val payload = join(encode(header.bytes), encode(claims.bytes))
    Algorithm.sign(header.algo, payload, key)
      .map(sig => join(payload, encode(sig)))
  }

  def unapply(str: String): Option[(Header, Claims, Array[Byte])] =
    if (str.indexOf(".") < 0) None else str.split("[.]", 3) match {
      case Array(headerStr, claimsStr, sigStr) =>
        for {
          Header(header) <- decode(headerStr).right.toOption
          Claims(claims) <- decode(claimsStr).right.toOption
          sig            <- decodeBytes(sigStr).right.toOption
        } yield (header, claims, sig)
      case _ =>
        None
    }

  /** verifies signature of unpacked jwt */
  def verify(
    jwt: (Header, Claims, Array[Byte]),
    algo: String,
    key: Algorithm.Key,
    leeway: FiniteDuration = NoLeeway): Option[(Header, Claims, Array[Byte])] =
    jwt match {
      case (header, claims, sig) =>
        // https://auth0.com/blog/2015/03/31/critical-vulnerabilities-in-json-web-token-libraries/
        if (algo != header.algo) None else {
          val payload = join(encode(header.bytes), encode(claims.bytes))
          def claimCheck = {
            val now = (System.currentTimeMillis() / 1000).seconds
            (claims.nbf.map(_ < (now + leeway)).getOrElse(true)
              && claims.exp.map(_ > (now - leeway)).getOrElse(true))
          }
          if (Algorithm.verify(header.algo, payload, key, sig) && claimCheck) Some(header, claims, sig)
          else None
        }
    }

  /** special case of verify where application is aware of key associated with jwt before unpacking */
  def verify(str: String, algo: String, key: Algorithm.Key): Option[(Header, Claims, Array[Byte])] =
    unapply(str).flatMap(verify(_, algo, key))
}
