package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
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
    Ok(views.html.login())
  }

  def signup = Action { implicit request =>
    Ok(views.html.signup())
  }

  def company = Action {
    Ok(views.html.company())
  }

}