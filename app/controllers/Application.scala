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

  def docs = Action { implicit request =>
    Ok(views.html.docs(getOptionUser))
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
        Panel.overview,
        Panel.overview_index,
        Panel.overview_usage,
        Panel.overview_account,
        Panel.overview_transaction,
        Panel.overview_api,
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