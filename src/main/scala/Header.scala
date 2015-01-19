package prints

import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods.{ compact, parseOpt, render }

/** https://tools.ietf.org/html/draft-ietf-oauth-json-web-token-32#section-5 */
trait Header {
  def algo: String
  def cty = get("cty")
  def typ = get("typ")
  def get(name: String): Option[String]
  def bytes: Array[Byte]
}

object Header {

  def apply(
    alg: String,
    typ: String = "JWT",
    cty: Option[String] = None,
    etc: Map[String, String] = Map.empty) = {
    val alg0 = alg
    val typ0 = typ
    val cty0 = cty
    new Header {
      private[this] val props: Map[String, String] =
        Map("alg" -> alg0, "typ" -> typ0) ++ etc ++ cty0.map(("cty" -> _))

      val algo = alg0

      lazy val bytes =
        compact(render((("alg" -> alg0) ~ ("typ" -> typ0) ~ ("cty" -> cty0) /: etc) {
          case (obj, (key, value)) =>
            obj.merge(JObject((key, JString(value)) :: Nil))
        })).getBytes("utf8")

      def get(name: String) = props.get(name)
    }
  }

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
