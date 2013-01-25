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
  val subscriptions: List[String] = List("individual", "startup", "company")

  def login() = Action { implicit request =>
    val f = Form(
      tuple(
        "email" -> Forms.email,
        "password" -> text
      )
    )
    val form = f.bindFromRequest
    if(form.hasErrors){
      Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"))
    }else{
      form.get match {
        case (email: String, pw: String) => {
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
      }
    }
  }

  def logout() = Action {
    Redirect(routes.Application.login).withNewSession
  }

  def signup() = Action { implicit request =>
    val f = Form(
      tuple(
        "email" -> Forms.email,
        "password" -> text,
        "name" -> text,
        "subscription" -> text
      )
    )

    val form = f.bindFromRequest
    if(form.hasErrors){
      Redirect(routes.Application.signup).flashing(
        "retry" -> "true",
        "error-basic" -> Messages("users.signup.fill")
      )
    }else{
      form.get match {
        case (email: String, pw: String, name: String, subscription: String) => {
          println(subscription)
          if(email.length == 0){
            Redirect(routes.Application.signup).flashing(
              "retry" -> "true", 
              "email" -> email, 
              "name" -> name, 
              "subscription" -> subscription, 
              "error-basic" -> Messages("users.signup.email")
            )
          }else{
            if(pw.length == 0){
              Redirect(routes.Application.signup).flashing(
                "retry" -> "true",
                "email" -> email, 
                "name" -> name, 
                "subscription" -> subscription, 
                "error-basic" -> Messages("users.signup.pw")
              )
            }else{
              if(name.length == 0){
                Redirect(routes.Application.signup).flashing(
                  "retry" -> "true", 
                  "email" -> email, 
                  "name" -> name, 
                  "subscription" -> subscription, 
                  "msg-basic" -> "", 
                  "error-basic" -> "", 
                  "error-detail" -> Messages("users.signup.name")
                )
              }else{
                if(Users.subscriptions.contains(subscription) != true){
                  Redirect(routes.Application.signup).flashing(
                    "retry" -> "true", 
                    "email" -> email, 
                    "name" -> name, 
                    "subscription" -> subscription, 
                    "msg-basic" -> "", 
                    "error-basic" -> "", 
                    "error-detail" -> Messages("users.signup.subscription")
                  )
                }else{
                  Redirect(routes.Application.login).flashing("retry" -> "true", "email" -> email, "message" -> Messages("users.signup.confirm"))
                }
              }
            }
          }
        }
      }
    }
  }

}