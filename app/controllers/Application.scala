package controllers

import play.api._
import play.api.mvc._

object Application extends Controller with Secured {
  
  def index = Action {
    Ok(views.html.overview())
  }

  def features = Action {
    Ok(views.html.features())
  }

  def docs = Action { implicit request =>
    Ok(views.html.docs())
  }

  def plans = Action {
    Ok(views.html.plans())
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

  def company = Action {
    Ok(views.html.company())
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
        Panel.order,
        Panel.pickup,
        Panel.support,
        Users.logout
      )
    ).as("text/javascript")
  }

}