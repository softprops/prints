package prints

import base64.Encode.urlSafe
import org.json4s.native.JsonMethods.parseOpt

object JWT {
 private[this] val dot = ".".getBytes

 private def encode(bytes: Array[Byte]): Array[Byte] =
   base64.Encode.urlSafe(bytes)

 private def decode(str: String) =
   base64.Decode.urlSafe(str).right.map(new String(_, "utf8"))

 def apply(header: Header, claims: Claims, key: Array[Byte]): Option[Array[Byte]] = {
   val payload = encode(header.bytes) ++ dot ++ encode(claims.bytes)   
   Algorithm.sign(header.algo, payload, key).map(payload ++ dot ++  _)
 }

 def unapply(str: String): Option[(Header, Claims, String)] = str.split("[.]") match {
   case Array(headerStr, claimsStr, sig) =>
     for {
       Header(header) <- decode(headerStr).right.toOption
       Claims(claims) <- decode(claimsStr).right.toOption
     } yield (header, claims, sig)
   case _ =>
     None
 }
}
