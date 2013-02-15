package controllers

import play.api._
import play.api.mvc._

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
        Async {
          MintpressoCore.getToken(user.id).map { res =>
            res.status match {
              case 200 =>
                Ok(views.html.docs.javascript.api(user, res.body))
              case 404 =>
                NotFound
              //case 403 =>
              case _ =>
                Forbidden
            }
          }
        }
      } getOrElse {
        Forbidden
      }
      case "javascript/test" => getOptionUser map { user =>
        Ok(views.html.docs.javascript.test(user))
      } getOrElse {
        Forbidden
      }
      case _ => NotFound
    }
  }

  def plans = Action { implicit request =>
    Ok(views.html.plans(getOptionUser))
  }

  def login = Action { implicit request =>
    if(authenticated){
      Redirect(routes.Panel.overview(getAccountId))
    }else{
      Ok(views.html.login())
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
        Panel.pickup,
        Panel.support,
        Users.logout
      )
    ).as("text/javascript")
  }

}