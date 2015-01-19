package prints

import org.json4s.native.JsonMethods.parseOpt

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4 */
trait Claims {
  def bytes: Array[Byte]
}

object Claims {
  def unapply(str: String): Option[Claims] =
    parseOpt(str).map { js =>
      new Claims {
        def bytes = str.getBytes("utf8")
      }
    }
}
