package prints

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

trait Algorithm {
  def apply(payload: Array[Byte], key: Array[Byte]): Array[Byte]
}

/** see also https://tools.ietf.org/html/draft-ietf-jose-json-web-algorithms-40#section-3.1 */
object Algorithm {
  val None: Algorithm = new Algorithm {
    val bytes = Array.empty[Byte]
    def apply(payload: Array[Byte], key: Array[Byte]): Array[Byte] =
      bytes
  }
  private def hmac(key: String, alg: String) =
    (key -> new HmacSha(alg))
  class HmacSha(alg: String) extends Algorithm {
    def apply(payload: Array[Byte], key: Array[Byte]): Array[Byte] = {
      val sec = new SecretKeySpec(key, alg)
      val mac = Mac.getInstance(alg)
      mac.init(sec)
      mac.doFinal(payload)
    }
  }
  val supported =
    (Map("none" -> Algorithm.None)
     + hmac("HS256", "HmacSHA256")
     + hmac("HS384", "HmacSHA384")
     + hmac("HS512", "HmacSHA512"))

  def apply(alg: String) = supported.get(alg)

  def sign(algo: String, payload: Array[Byte], key: Array[Byte]) =
    supported.get(algo).map(_.apply(payload, key))
}
