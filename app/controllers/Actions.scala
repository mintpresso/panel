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

  def Signed(f: Request[AnyContent] => Result) = Action { request =>
    request.session.get("accountId").map { id =>
      f(request)
    } getOrElse {
      Results.Forbidden
    }
  }

}