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
  val server = "http://localhost:9001"
  val versionPrefix = "/v1"
  val urls: Map[String, String] = Map(
      "authenticate" -> (versionPrefix + "/account/authenticate"),
      "addAccount" -> (versionPrefix + "/account"),
      "getAccount" -> (versionPrefix + "/v1")
    )
  def addAccount(email: String, password: String, name: String): Future[Response] = {
    WS.url(server + urls("addAccount"))
      .withQueryString(("email", email), ("password", password), ("name", name))
      .post(Map("key" -> Seq("value")))
  }
  def authenticate(email: String, password: String): Future[Response] = {
    WS.url(server + urls("authenticate"))
      .withQueryString(("email", email), ("password", password))
      .post(Map("key" -> Seq("value")))
  }
  
  def get(json: JsObject) = {
    
  }
}