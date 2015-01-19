package prints

import org.json4s.native.JsonMethods.{ compact, parseOpt, render }
import org.json4s._
import org.json4s.JsonDSL._

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4 */
trait Claims {
  // register claim names https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4.1

  def iss = get("iss")
  def sub = get("sub")
  def aud = get("aud")
  def exp = get("exp")
  def nbf = get("nbf")
  def iat = get("iat")
  def jti = get("jti")

  def get(name: String): Option[String]

  def bytes: Array[Byte]
}

object Claims {

  private def bytes(json: JValue) =
    compact(render(json)).getBytes("utf8")

  def unapply(str: String): Option[Claims] =
    parseOpt(str).flatMap { js =>
      (for { JObject(obj) <- js } yield obj).headOption
    }.map { obj =>
      new Claims {
        def get(name: String) = (for {
          (`name`, JString(value)) <- obj
        } yield value).headOption

        lazy val bytes = str.getBytes("utf8")
      }
    }

  def apply(values: (String, String)*): Claims =
    new Claims {
      private[this] val map = values.toMap
      lazy val bytes = Claims.bytes((JObject(Nil) /: map) {
        case (obj, (key, value)) =>
          val that = JObject(JField(key, JString(value)) :: Nil)
          obj.merge(that)
      })

      def get(name: String) = map.get(name)
    }

  def apply(values: JValue): Claims =
    new Claims {
      lazy val bytes = Claims.bytes(values)
      def get(name: String): Option[String] = (for {
        JObject(obj)             <- values
        (`name`, JString(value)) <- obj
      } yield value).headOption
    }
}
