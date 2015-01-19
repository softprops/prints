package prints

import org.json4s._
import org.json4s.native.JsonMethods.parseOpt

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-5 */
trait Header {
  def algo: String
  def cty = get("cty")
  def typ = get("typ")
  def get(name: String): Option[String]
  def bytes: Array[Byte]
}

object Header {
  def unapply(str: String): Option[Header] = {
    parseOpt(str).flatMap { js =>      
     (for {
       JObject(obj)          <- js
       ("alg", JString(alg)) <- obj
     } yield (obj, alg)).headOption
    }.map { case (obj, alg) =>
      new Header {
        def algo = alg
        def get(name: String) = (for {
          (`name`, JString(value)) <- obj
        } yield value).headOption
        def bytes = str.getBytes("utf8")
      }
    }
  }
}
