package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.Play.current
import play.api.libs._
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
  private var accountId: Int = -1

  def getAccountId(): Int = accountId
  def authenticated(implicit request: RequestHeader): Boolean = {
  	request.session.get("accountId").map { id =>
  	  accountId = id.toInt
  	  true
  	} getOrElse {
  	  false
  	}
  }
  def Signed(f: Request[AnyContent] => Result) = Action { implicit request =>
    if(authenticated){
      f(request)
    }else{
      Results.Forbidden
    }
  }

  def SignedAccount(accessId: Int)(f: Request[AnyContent] => Result) = Action { implicit request =>
    if(authenticated && (accountId == accessId)){
        f(request)
    }else{
      Results.Forbidden
    }
  }

}