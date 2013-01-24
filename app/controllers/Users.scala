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

object Users extends Controller {

  def login() = Action { implicit request =>
    val form = Form(
      tuple(
        "email" -> text,
        "password" -> text
      )
    )
    val (email: String, pw: String) = form.bindFromRequest.get
    println(email + pw)
    if(email == "eces@mstock.org"){
      if(pw == "jin"){
          val accountId = 1
          Redirect(routes.Panel.overview(accountId)).withSession("accountId" -> accountId.toString, "email" -> email)
        }else{
          Redirect(routes.Application.login).flashing("retry" -> "true", "email" -> email, "error" -> Messages("users.login.pw"))
        }
    }else{
      Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.email"))
    }
  }

  def logout() = Action {
    Redirect(routes.Application.login).withNewSession
  }

}