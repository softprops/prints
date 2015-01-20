package prints

import java.security.interfaces.RSAPrivateKey
import java.security.Signature
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

trait Algorithm {
  def apply(payload: Array[Byte], key: Algorithm.Key): Array[Byte]
}

/** see also https://tools.ietf.org/html/draft-ietf-jose-json-web-algorithms-40#section-3.1 */
object Algorithm {
  trait Key
  object Key {
    case object None extends Key
    case class Bytes(bytes: Array[Byte]) extends Key
    case class Rsa(pk: RSAPrivateKey) extends Key
  }


  val None: Algorithm = new Algorithm {
    val bytes = Array.empty[Byte]
    def apply(payload: Array[Byte], key: Key): Array[Byte] =
      bytes
  }
  private def hmac(key: String, alg: String) =
    (key -> new HmacSha(alg))
  private def rsa(key: String, alg: String) =
    (key -> new RsaSig(alg))
  class HmacSha(alg: String) extends Algorithm {
    def apply(payload: Array[Byte], key: Key): Array[Byte] =
      key match {
        case Key.Bytes(keyBytes) =>
          val sec = new SecretKeySpec(keyBytes, alg)
          val mac = Mac.getInstance(alg)
          mac.init(sec)
          mac.doFinal(payload)
        case _ =>
          // todo: fail
          Array.empty[Byte]
      }
  }
  class RsaSig(alg: String) extends Algorithm {
    def apply(payload: Array[Byte], key: Key): Array[Byte] =
      key match {
        case Key.Rsa(pk) =>
          val sig = Signature.getInstance(alg)
          sig.initSign(pk)
          sig.update(payload)
          sig.sign()
        case _ =>
          // todo: fail
          Array.empty[Byte]
      }
  }
  val supported =
    (Map("none" -> Algorithm.None)
     + hmac("HS256", "HmacSHA256")
     + hmac("HS384", "HmacSHA384")
     + hmac("HS512", "HmacSHA512")
     + rsa("RS256", "SHA256withRSA")
     + rsa("RS384", "SHA384withRSA")
     + rsa("RS512", "SHA512withRSA"))

  def apply(alg: String) = supported.get(alg)

  def sign(algo: String, payload: Array[Byte], key: Algorithm.Key) =
    supported.get(algo).map(_.apply(payload, key))
}
