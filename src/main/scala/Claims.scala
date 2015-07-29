package prints

import org.json4s.native.JsonMethods.{ compact, parseOpt, render }
import org.json4s._
import org.json4s.JsonDSL._
import scala.util.control.Exception.allCatch
import scala.concurrent.duration._

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4 */
trait Claims {

  /** @return a option of the json field matching the provided predicate */
  def get(f: JField => Boolean): Option[JValue]

  /** @return a representation of this claim as utf8 bytes */
  def bytes: Array[Byte]

  // registered claim names https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-4.1

  def iss = str("iss")
  def sub = str("sub")
  def aud = str("aud")
  def exp = seconds("exp")
  def nbf = seconds("nbf")
  def iat = seconds("iat")
  def jti = str("jti")

  def seconds(name: String): Option[FiniteDuration] =
    long(name).map(_.seconds)

  def int(name: String): Option[Int] =
    long(name).map(_.toInt)

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
}

object Claims {

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

  /** factory for simple string -> string claims */
  def apply(values: (String, String)*): Claims =
    apply(values.toMap)

  /** factory for json claims */
  def apply(values: JValue): Claims =
    new Claims {
      lazy val bytes = compact(render(values)).getBytes("utf8")
      def get(f: JField => Boolean): Option[JValue] =
        values.findField(f).map {
          case (_, value) => value
        }
    }
}
