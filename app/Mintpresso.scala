package com.mintpresso

import play.api._
import play.api.i18n.Messages
import play.api.data.Forms._
import play.api.data._
import play.api.Play.current
import play.api.libs._
import play.api.libs.ws._
import play.api.cache._
import play.api.libs.json._
import play.api.libs.iteratee._
import scala.concurrent.stm._
import scala.concurrent._

object MintpressoCore {
  val server = "http://192.168.0.5:9001"
  val initial = "Play 2.1 Core API"
  val versionPrefix = "/v1"
  val urls: Map[String, String] = Map(
    "authenticate" -> (versionPrefix + "/account/authenticate"),
    "addAccount" -> (versionPrefix + "/account"),
    "getAccount" -> (versionPrefix + "/account/%d"),
    "getToken" -> (versionPrefix + "/account/%d/token"),
    "updateToken" -> (versionPrefix + "/account/%d/token")
  )
  val Type: Map[String, Long] = Map(
      "user" -> 10,
      "page" -> 20,
      "post" -> 30
    )
  val TypeString: Map[Long, String] = Map(
      10L -> "user",
      20L -> "page",
      30L -> "post"
    )

  val Types: List[String] = List("user", "page", "post")

  def addAccount(email: String, password: String, name: String): Future[Response] = {
    WS.url(server + urls("addAccount"))
      .withQueryString(("email", email), ("password", password), ("name", name))
      .withHeaders( ("X-Requested-With", initial) )
      .post(Map("key" -> Seq("value")))
  }
  def authenticate(email: String, password: String): Future[Response] = {
    WS.url(server + urls("authenticate"))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(("email", email), ("password", password))
      .post(Map("key" -> Seq("value")))
  }
  def getToken(id: Int): Future[Response] = {
    WS.url(server + urls("getToken").format(id))
      .withHeaders( ("X-Requested-With", initial) )
      .get()
  }

  def setToken(id: Int, password: String, url: List[String]): Future[Response] = {
    WS.url(server + urls("setToken").format(id))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(("pw", password), ("url", url.mkString("|")))
      .put(Map("key" -> Seq("value")))
  }
}

object MintpressoAPI {
  var connections: Map[String, Mintpresso] = Map()
  def apply(label: String, accountId: Int, token: String): Mintpresso = {
    // API Token consists of {api token}::{account id}
    val tokens = token.split("::")
    if(!connections.contains(label)){
      val m: Mintpresso = new Mintpresso(accountId, tokens(0))
      connections += ((label, m))
    }
    connections(label)
  }
}
  val server = "http://localhost:9001"
class Mintpresso(accId: Int, token: String) {
  val initial = "Play 2.1 API"
  val versionPrefix = "/v1"
  val urls: Map[String, String] = Map(
    "getPoint" -> (versionPrefix + "/account/%d/point/%d"),
    "getPointType" -> (versionPrefix + "/account/%d/points/type"),
    "getLatestPoint" -> (versionPrefix + "/account/%d/points/latest"),
    "getPointByTypeOrIdentifier" -> (versionPrefix + "/account/%d/point"),
    "addPoint" -> (versionPrefix + "/account/%d/point"),
    "findEdges" -> (versionPrefix + "/account/%d/edge")
  )

  def getPoint(id: Int): Future[Response] = {
    WS.url(server + urls("getPoint").format(accId, id))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString( ("api_token", token) )
      .get()
  }
  def getPointTypes(): Future[Response] = {
    WS.url(server + urls("getPointType").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString( ("api_token", token) )
      .get() 
  }
  def getLatestPoints(): Future[Response] = {
    WS.url(server + urls("getLatestPoint").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString( ("api_token", token) )
      .get()  
  }
  def findByType(typeString: String, limit: Int = 30, offset: Int = 0): Future[Response] = {
    WS.url(server + urls("getPointByTypeOrIdentifier").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(("api_token", token), ("type", typeString), ("limit", limit.toString), ("offset", offset.toString))
      .get()
  }
  def findByIdentifier(identifier: String, limit: Int = 30, offset: Int = 0): Future[Response] = {
    WS.url(server + urls("getPointByTypeOrIdentifier").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(("api_token", token), ("identifier", identifier), ("limit", limit.toString), ("offset", offset.toString))
      .get()
  }
  def findByTypeAndIdentifier(typeString: String, identifier: String, limit: Int = 30, offset: Int = 0): Future[Response] = {
    WS.url(server + urls("getPointByTypeOrIdentifier").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(("api_token", token), ("type", typeString), ("identifier", identifier), ("limit", limit.toString), ("offset", offset.toString))
      .get()
  }
  def addPoint(typeString: String, identifier: String, json: String) = {
    var p1: String = ""
    var p2: String = ""
    if(identifier.length > 0){
      p1 = "\"identifier\": \"%s\",".format(identifier)
    }
    if(json.length > 0){
      p2 = "\"data\": %s,".format(json)
    }
    val body = 
    """
{
  "point": {
    %s %s
    "type": "%s"
  }
}
    """.format(p1, p2, typeString)
    WS.url(server + urls("addPoint").format(accId))
      .withHeaders( ("Content-Type", "application/json"), ("X-Requested-With", initial) )
      .withQueryString( ("api_token", token) )
      .post[String](body)
  }
  def findRelations(query: Map[String, String]) = {
    val queries = query + (("api_token" -> token))
    WS.url(server + urls("findEdges").format(accId))
      .withHeaders( ("X-Requested-With", initial) )
      .withQueryString(queries.toSeq:_*)
      .get()
  }
}