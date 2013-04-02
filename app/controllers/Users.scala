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
import models.User
import play.api.libs.concurrent.Execution.Implicits._

object Users extends Controller with Secured {
  def login() = Action { implicit request =>
    val f = Form(
      tuple(
        "email" -> Forms.email,
        "password" -> text,
        "redirect_url" -> optional(text)
      )
    )
    var email = ""
    try {
      f.bindFromRequest.fold (
        formWithErrors => {
          var savedEmail = ""
          formWithErrors.error("email") match {
            case None => {
              savedEmail = formWithErrors.data("email")
            }
            case _ => 
          }
          Redirect(routes.Application.login)
          .flashing("retry" -> "true", "error" -> Messages("users.login.fill"), "msg" -> "", "email" -> savedEmail)
        },
        values => {
          var (email, pw, redirectUrl) = values
          if(email.trim().length == 0){
            throw new Exception(Messages("users.login.fill"))
          }
          if(pw.trim().length == 0){
            throw new Exception(Messages("users.login.fill"))
          }
          Async {
            MintpressoAPI("internal").findByTypeAndIdentifier("user", email).map { response =>
              response.status match {
                case 200 => {
                  (response.json \ "point").asOpt[JsObject].map { obj =>
                    val _id = (obj \ "id").as[Int]
                    val _email = (obj \ "identifier").as[String]
                    val _name = (obj \ "data" \ "name").as[String]
                    val _pw = (obj \ "data" \ "password").as[String]
                    if(_pw == Crypto.sign(pw)){
                      Redirect(routes.Users.postAuth)
                        .withSession(
                          "id" -> _id.toString,
                          "email" -> _email,
                          "name" -> _name
                        ).flashing(
                          "redirectUrl" -> redirectUrl.getOrElse(routes.Panel.overview(_id).url)
                        )
                    }else{
                      Redirect(routes.Application.login)
                        .flashing("retry" -> "true", "error" -> Messages("users.login.pw"), "msg" -> "", "email" -> email)
                    }
                  } getOrElse {
                    Redirect(routes.Application.login)
                      .flashing("retry" -> "true", "error" -> Messages("users.login.what"), "msg" -> "", "email" -> email)
                  }
                }
                case _ => {
                  Redirect(routes.Application.login)
                    .flashing("retry" -> "true", "error" -> Messages("users.login.email"), "msg" -> "")
                }
              }
            }
          }
        } 
      )
    } catch {
      case e: Exception =>
        Redirect(routes.Application.login)
          .flashing("retry" -> "true", "error" -> e.getMessage, "msg" -> "", "email" -> email)
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
        "password" -> nonEmptyText,
        "name" -> nonEmptyText,
        "subscription" -> nonEmptyText
      )
    )
    try {
      f.bindFromRequest.fold (
        form => {
          var msg = Messages("users.signup.fill")
          Redirect(routes.Application.signup).flashing(
            "retry" -> "true", 
            "subscription" -> (form.error("subscription") match {
              case None => form.data("subscription")
              case _ => 
                msg = Messages("users.signup.subscription")
                ""
            }),
            "name" -> (form.error("name") match {
              case None => form.data("name")
              case _ => 
                msg = Messages("users.signup.name")
                ""
            }),
            "password" -> (form.error("password") match {
              case None => form.data("password")
              case _ =>
                msg = Messages("users.signup.pw")
                ""
            }),
            "email" -> (form.error("email") match {
              case None => form.data("email")
              case _ => 
                msg = Messages("users.signup.email")
                ""
            }), 
            "error-basic" -> msg
          )
        },
        values => {
          var (email, pw, name, subscription) = values
          
          Async {
            val data = Json.obj(
                "name" -> name,
                "password" -> Crypto.sign(pw),
                "subscription" -> subscription
              )
            MintpressoAPI("internal").addPoint("user", email, Json.stringify(data)).map { response =>
              response.status match {
                case 200 => {
                  Redirect(routes.Application.signup).flashing(
                    "retry" -> "true", 
                    "email" -> email, 
                    "name" -> name, 
                    "subscription" -> subscription,  
                    "msg-detail" -> "", 
                    "error-basic" -> Messages("users.signup.email.duplicated", email), 
                    "error-detail" -> ""
                  )  
                }
                case 201 => {
                  val json = Json.parse(response.body)
                  val id = (json \ "point" \ "id").as[Int]
                  if(User.createToken(id, email, List("*"), "default") == true){
                    Redirect(routes.Application.login).flashing("retry" -> "true", "email" -> email, "msg" -> Messages("users.signup.confirm"))
                  }else{
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
                case 400 => {
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

                case _ => {
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
      )
    } catch {
      case e: Exception =>
        Redirect(routes.Application.signup).flashing(
          "retry" -> "true", 
          "error-basic" -> e.getMessage
        )
    }
  }
}