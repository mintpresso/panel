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

  def docs = Action {
    Ok(views.html.docs())
  }

  def plans = Action {
    Ok(views.html.plans())
  }

  def login = Action {
    Ok(views.html.login())
  }

  def company = Action {
    Ok(views.html.company())
  }

}