package models

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
  def createToken(id: Int, email: String, urls: List[String], name: String): Boolean = {
    val urlString = urls.mkString("|")
    val key: String = id.toString + java.util.UUID.randomUUID()
    val data = Json.obj(
      "name" -> name,
      "expired" -> false,
      "url" -> urlString
    )
    
    val f1 = MintpressoAPI("internal").addPoint("token", key, Json.stringify(data))
    val res1 = Await.result(f1, 3 seconds)
    res1.status match {
      case 201 => {
        val query = Map(  ("subjectId" -> email),
                      ("subjectType" -> "user"),
                      ("verb" -> "issue"),
                      ("objectId" -> key),
                      ("objectType" -> "token"))

        val f2 = MintpressoAPI("internal").linkWithEdge(query)
        val res2 = Await.result(f2, 3 seconds)
        res2.status match {
          case 201 => true
          case _ => {
            println("User token not linked")
            false
          }
        }

      }
      case _ => {
        println("User token not created")
        false
      }
    }
  }
  def findTokens(id: Int, email: String) = {
    val query = Map(  "subjectIdentifier" -> email,
                      "subjectType" -> "user",
                      "verb" -> "issue",
                      "objectType" -> "token")
    val f1 = MintpressoAPI("internal").findRelations(query)
    val r1 = Await.result(f1, 3 seconds)
    r1.status match {
      case 200 => {
        // opt for single token
        val id = ((r1.json \ "edges").as[JsArray].value(0) \ "objectId").as[Int]
        val f2 = MintpressoAPI("internal").getPoint(id.toInt)
        val r2 = Await.result(f2, 3 seconds)
        (r2.json \ "point").as[JsValue]
      }
      case _ => Json.parse("{}")
    }
    
  }
  def updateToken(key: String, urls: List[String]) = {
    false
  }
  def deleteToken(key: String) = {
    false
  }
}