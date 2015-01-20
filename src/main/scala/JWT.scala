package prints

import base64.Encode.urlSafe
import org.json4s.native.JsonMethods.parseOpt

object JWT {
  private[this] val dot = ".".getBytes

  private def encode(bytes: Array[Byte]): Array[Byte] =
    base64.Encode.urlSafe(bytes)

  private def decode(str: String) =
    base64.Decode.urlSafe(str).right.map(new String(_, "utf8"))

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
    key: Array[Byte]): Option[Array[Byte]] = {
    val payload = join(encode(header.bytes), encode(claims.bytes))
    Algorithm.sign(header.algo, payload, key)
      .map(sig => join(payload, encode(sig)))
  }

  def unapply(str: String): Option[(Header, Claims, String)] =
    if (str.indexOf(".") < 0) None else str.split("[.]", 3) match {
      case Array(headerStr, claimsStr, sig) =>
        for {
          Header(header) <- decode(headerStr).right.toOption
          Claims(claims) <- decode(claimsStr).right.toOption
        } yield (header, claims, sig)
      case _ =>
        None
    }
}
