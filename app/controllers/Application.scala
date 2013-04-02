package controllers

import play.api._
import play.api.mvc._

import models.User

object Application extends Controller with Secured {
  
  def index = Action { implicit request =>
    Ok(views.html.overview(getOptionUser))
  }

  def features = Action { implicit request =>
    Ok(views.html.features(getOptionUser))
  }

  def docsIndex = docs("index")
  def docs(page: String = "index") = Action { implicit request =>
    import com.mintpresso._
    import play.api.libs.concurrent.Execution.Implicits._

    page match {
      case "index" => Ok(views.html.docs.index(getOptionUser))
      case "javascript/api" => getOptionUser map { user =>
        Ok(views.html.docs.javascript.api(user, User.findTokens(getId, getUser.email).toString))
      } getOrElse {
        Results.Redirect(routes.Application.login).flashing(
          "msg" -> "해당 링크에 들어가려면 로그인 인증이 필요합니다.",
          "redirect_url" -> request.path
        )
      }
      case "javascript/test" => getOptionUser map { user =>
        Ok(views.html.docs.javascript.test(user))
      } getOrElse {
        Results.Redirect(routes.Application.login).flashing(
          "msg" -> "해당 링크에 들어가려면 로그인 인증이 필요합니다.",
          "redirect_url" -> request.path
        )
      }
      case _ => NotFound
    }
  }

  def plans = Action { implicit request =>
    Ok(views.html.plans(getOptionUser))
  }

  def login = Action { implicit request =>
    if(flash.get("account_change").getOrElse("") == "true"){
      Ok(views.html.changeAccount())
    }else{
      if(authenticated){
        Redirect(routes.Panel.overview(getId))
      }else{
        Ok(views.html.login())
      }
    }
  }

  def signup = Action { implicit request =>
    Ok(views.html.signup())
  }

  def company = Action { implicit request =>
    Ok(views.html.company(getOptionUser))
  }

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("routes")(
        routes.javascript.Application.docs,
        routes.javascript.Application.javascriptValues,
        Panel.overview,
        Panel.overview_index,
        Panel.overview_usage,
        Panel.overview_account,
        Panel.overview_transaction,
        Panel.overview_api,
        Panel.overview_api_set,
        Panel.data,
        Panel.data_index,
        Panel.data_view,
        Panel.data_filter,
        Panel.data_import,
        Panel.data_import_add,
        Panel.data_export,
        Panel.order,
        Panel.order_index,
        Panel.pickup,
        Panel.pickup_index,
        Panel.support,
        Panel.support_index,
        Panel.support_conversation,
        Panel.support_consulting,
        Users.logout
      )
    ).as("text/javascript")
  }

  def javascriptValues = Action { implicit request =>
    var kv: Map[String, String] = Map()
    getOptionUser map { user =>
      kv += (("id" -> user.id.toString))
      kv += (("email" -> user.email))
      kv += (("name" -> user.name))
    } getOrElse {

    }
    Ok( views.html.javascriptValues(kv) ).as("text/javascript")
  }

}