package controllers

import play.api._
import play.api.i18n.Messages
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import play.api.Play.current
import play.api.libs._
import play.api.cache._
import play.api.libs.json._

object Panel extends Controller with Secured {
  def overview(accountId: Int) = Signed { implicit request =>
  	Ok("overview " + accountId)
  }
}