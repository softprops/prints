package prints

import org.json4s.native.JsonMethods.{ compact, parseOpt, render }
import org.json4s._
import org.json4s.JsonDSL._
import scala.util.control.Exception.allCatch

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4 */
trait Claims {
  // registered claim names https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4.1

  def iss = str("iss")
  def sub = str("sub")
  def aud = str("aud")
  def exp = long("exp")
  def nbf = long("nbf")
  def iat = long("iat")
  def jti = str("jti")

  def long(name: String): Option[Long] =
    get(_ match {
      case (key, JInt(value)) => key == name
      case _ => false
    })
    .collect {
      case JInt(value) =>
        value.toLong
    }

  def str(name: String): Option[String] =
    get(_ match {
      case (key, JString(value)) => key == name
      case _ => false
    })
    .collect {
      case JString(value) =>
        value
    }

  def get(f: JField => Boolean): Option[JValue]

  def bytes: Array[Byte]
}

object Claims {

  private def bytes(json: JValue) =
    compact(render(json)).getBytes("utf8")

  def unapply(s: String): Option[Claims] =
    parseOpt(s).map { obj =>
      new Claims {
        def get(f: JField => Boolean): Option[JValue] =
          obj.findField(f).map {
            case (_, value) => value
          }

        lazy val bytes = s.getBytes("utf8")
      }
    }

  def apply(values: (String, String)*): Claims =
    apply((JObject(Nil) /: values) {
        case (obj, (key, value)) =>
          val that = JObject(JField(key, JString(value)) :: Nil)
          obj.merge(that)
      })

  def apply(values: JValue): Claims =
    new Claims {
      lazy val bytes = Claims.bytes(values)
      def get(f: JField => Boolean): Option[JValue] =
        values.findField(f).map {
          case (_, value) => value
        }
    }
}
