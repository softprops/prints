package prints

import java.util.Arrays
import java.security.interfaces.{ RSAPublicKey, RSAPrivateKey }
import java.security.Signature
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

trait Algorithm {
  def sign(payload: Array[Byte], key: Algorithm.Key): Array[Byte]
  def verify(payload: Array[Byte], key: Algorithm.Key, sig: Array[Byte]): Boolean
}

/** see also https://tools.ietf.org/html/draft-ietf-jose-json-web-algorithms-40#section-3.1 */
object Algorithm {
  trait Key
  object Key {
    case object None extends Key
    case class Bytes(bytes: Array[Byte]) extends Key
    case class Rsa(pubKey: RSAPublicKey, privKey: RSAPrivateKey) extends Key
  }


  val None: Algorithm = new Algorithm {
    val bytes = Array.empty[Byte]
    def sign(payload: Array[Byte], key: Key): Array[Byte] =
      bytes
    def verify(payload: Array[Byte], key: Key, sig: Array[Byte]): Boolean =
      sig.length == 0
  }
  private def hmac(key: String, alg: String) =
    (key -> new HmacSha(alg))
  private def rsa(key: String, alg: String) =
    (key -> new RsaSig(alg))
  class HmacSha(alg: String) extends Algorithm {
    def sign(payload: Array[Byte], key: Key): Array[Byte] =
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
    def verify(payload: Array[Byte], key: Key, sig: Array[Byte]): Boolean =
      Arrays.equals(sign(payload, key), sig)
  }

  class RsaSig(alg: String) extends Algorithm {
    def sign(payload: Array[Byte], key: Key): Array[Byte] =
      key match {
        case Key.Rsa(_, priv) =>
          val sig = Signature.getInstance(alg)
          sig.initSign(priv)
          sig.update(payload)
          sig.sign()
        case _ =>
          // todo: fail
          Array.empty[Byte]
      }
    def verify(payload: Array[Byte], key: Key, sig: Array[Byte]): Boolean =
      key match {
        case Key.Rsa(pub, _) =>
          val ver = Signature.getInstance(alg)
          ver.initVerify(pub)
          ver.update(payload)
          ver.verify(sig)
        case _ => false
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

  def sign(algo: String, payload: Array[Byte], key: Algorithm.Key): Option[Array[Byte]] =
    supported.get(algo).map(_.sign(payload, key))

  def verify(algo: String, payload: Array[Byte], key: Algorithm.Key, sig: Array[Byte]): Boolean =
    supported.get(algo).exists(_.verify(payload, key, sig))
}
