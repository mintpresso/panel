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

import com.mintpresso._
import play.api.libs.concurrent.Execution.Implicits._

object Users extends Controller with Secured {
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
      Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "")
    }else{
      form.get match {
        case (email: String, pw: String) => {
          if(email.trim().length == 0){
            Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "")
          }else if(pw.trim().length == 0){
            Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "", "email" -> email)
          }else{
            Async {
              MintpressoCore.authenticate(email, pw).map { response =>
                response.status match {
                  case 200 => {
                    (response.json \ "account").asOpt[JsObject].map { obj =>
                      val id = (obj \ "id").as[Int]
                      val name = (obj \ "name").as[String]
                      val email = (obj \ "email").as[String]
                      val api_token = (obj \ "api_token").as[String]
                      Redirect(routes.Users.postAuth)
                        .withSession(
                          "accountId" -> id.toString,
                          "name" -> name,
                          "email" -> email,
                          "apiToken" -> api_token
                        ).flashing(
                          "redirectUrl" -> routes.Panel.overview(id).url
                        )
                    } getOrElse {
                      Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "")
                    }
                  }
                  case 204 => {
                    Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.email"), "msg" -> "")
                  }
                  case _ => {
                    Redirect(routes.Application.login).flashing("retry" -> "true", "email" -> email, "error" -> Messages("users.login.pw"), "msg" -> "")
                  }
                }
              }
            }
          }
        }
        case _ => Redirect(routes.Application.login).flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "")
      }
    }
  }

  def postAuth() = Action { implicit request =>
    Ok(views.html.loginPost(getUser))
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
                  Async {
                    MintpressoCore.addAccount(email, pw, name).map { response =>
                      (response.json \ "status").asOpt[JsObject].map { obj =>
                        (obj \ "code").asOpt[Int].getOrElse(0) match {
                          case 201 => Redirect(routes.Application.login).flashing("retry" -> "true", "email" -> email, "msg" -> Messages("users.signup.confirm"))
                          case 409 => {
                            Redirect(routes.Application.signup).flashing(
                              "retry" -> "true", 
                              "email" -> email, 
                              "name" -> name, 
                              "subscription" -> subscription,  
                              "msg-detail" -> "", 
                              "error-basic" -> Messages("users.signup.email.duplicated", (obj \ "message").asOpt[String].getOrElse("  ")), 
                              "error-detail" -> ""
                            )  
                          }
                          case 500 => {
                            Redirect(routes.Application.signup).flashing(
                              "retry" -> "true", 
                              "email" -> email, 
                              "name" -> name, 
                              "subscription" -> subscription, 
                              "msg-basic" -> "", 
                              "error-basic" -> "", 
                              "error-detail" -> Messages("server.500")
                            )  
                          }

                          // case 400
                          case _ => {
                            Redirect(routes.Application.signup).flashing(
                              "retry" -> "true", 
                              "email" -> email, 
                              "name" -> name, 
                              "subscription" -> subscription, 
                              "msg-basic" -> "", 
                              "error-basic" -> "", 
                              "error-detail" -> Messages("server.400")
                            )  
                          }
                        }
                      } getOrElse {
                        Redirect(routes.Application.signup).flashing(
                          "retry" -> "true", 
                          "email" -> email, 
                          "name" -> name, 
                          "subscription" -> subscription, 
                          "msg-basic" -> "", 
                          "error-basic" -> "", 
                          "error-detail" -> Messages("users.signup.retry")
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}