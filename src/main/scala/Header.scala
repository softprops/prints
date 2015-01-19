package prints

import org.json4s._
import org.json4s.native.JsonMethods.parseOpt

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-5 */
trait Header {
  def algo: String
  def bytes: Array[Byte]
}

object Header {
  def unapply(str: String): Option[Header] = {
    parseOpt(str).flatMap { js =>      
     (for {
       JObject(obj)          <- js
       ("alg", JString(alg)) <- obj
     } yield alg).headOption
    }.map { alg =>
      new Header {
        def algo = alg
        def bytes = str.getBytes("utf8")
      }
    }
  }
}
