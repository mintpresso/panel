package models

import play.api.{Play, Logger}
import play.api.mvc._
import play.api.Play.current
import play.api.libs._
import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.concurrent._
import scala.concurrent._
import scala.concurrent.duration._

import com.mintpresso._
import play.api.libs.concurrent.Execution.Implicits._

case class Token(key: String, data: String)
case class User(id: Int, email: String, name: String)
object User {
  val mintpresso: Affogato = Affogato( Play.configuration.getString("mintpresso.internal.api").getOrElse(""), Play.configuration.getString("mintpresso.internal.id").getOrElse("0").toLong)

  def createToken(id: Int, email: String, urls: List[String], name: String)(implicit request: RequestHeader): Boolean = {
    val urlString = urls.mkString("|")
    val key: String = id.toString + java.util.UUID.randomUUID()
    val data = Json.obj(
      "name" -> name,
      "expired" -> false,
      "url" -> urlString,
      "domain" -> ""
    )
    
    val p = Point(0L, "token", key, Json.stringify(data), "", 0, 0, 0)
    mintpresso.set(p) match {
      case Some(p) => {
        // true or false
        mintpresso.set("user", email, "issue", "token", p.identifier)
      }
      case None => {
        val uuid = java.util.UUID.randomUUID().toString
        val p = Point(0L, "warning", uuid, Json.obj(
            "message" -> "token not created",
            "domain" -> request.domain,
            "remoteAddress" -> request.remoteAddress,
            "url" -> request.uri,
            "user_email" -> email,
            "token_not_created" -> key
          ).toString, "", 0, 0, 0)

        mintpresso.set(p) match {
          case Some(point) => mintpresso.set("user", "support@mintpresso.com", "log", "warning", uuid)
          case None => Logger.info("Not logged. User("+email+") token not created.")
        }
        false
      }
    }
  }
  def findTokens(id: Int, email: String) = {
    mintpresso.get(Some(id.toLong), "user", "", "issue", None, "token", "") match {
      case Some(edges: List[Edge]) => {
        val e = edges(0)
        val token = mintpresso.get(e._object.id).get
        Json.obj(
          "id" -> token.id,
          "identifier" -> token.identifier,
          "type" -> token._type,
          "data" -> Json.parse(token.data),
          "createdAt" -> token.createdAt.toLong
        )
      }
      case None => {
        Json.parse("{}")
      }
    }
  }
  def updateToken(key: String, urls: List[String]) = {
    false
  }
  def deleteToken(key: String) = {
    false
  }
}