package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.Play.current
import play.api.libs._
import models.User

/** Uncomment the following lines as needed **/
/**
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import java.util.concurrent._
import scala.concurrent.stm._
import akka.util.duration._
import play.api.cache._
import play.api.libs.json._
**/

trait Secured {
  def getId(implicit request: RequestHeader): Int = {
    request.session.get("id").getOrElse(-1).toString.toInt
  }
  def getIdentifier(implicit request: RequestHeader): String = {
    request.session.get("email").getOrElse("anonymous@mintpresso.com")
  }
  def getUser(implicit request: RequestHeader): User = {
    User(
      request.session.get("id").getOrElse(-1).toString.toInt,
      request.session.get("email").getOrElse("anonymous@mintpresso.com"),
      request.session.get("name").getOrElse("Unknown")
    )
  }
  def getOptionUser(implicit request: RequestHeader): Option[User] = {
    request.session.get("id").map { id =>
      Some(getUser)
    } getOrElse {
      None
    }
  }
  def authenticated(implicit request: RequestHeader): Boolean = {
  	request.session.get("id").map { id =>
  	  true
  	} getOrElse {
  	  false
  	}
  }
  def Signed(f: Request[AnyContent] => Result) = Action { implicit request =>
    if(authenticated){
      f(request)
    }else{
      Results.Redirect(routes.Application.login).flashing(
        "msg" -> "해당 링크에 들어가려면 로그인 인증이 필요합니다.",
        "redirect_url" -> request.path
      )
    }
  }

  def SignedAccount(accessId: Int)(f: Request[AnyContent] => Result) = Action { implicit request =>
    if(getId == accessId){
      f(request)
    }else{
      Results.Redirect(routes.Application.login).flashing(
        "msg" -> "해당 링크에 들어가려면 로그인 인증이 필요합니다.",
        "redirect_url" -> request.path,
        "account_change" -> "true"
      )
    }
  }

}