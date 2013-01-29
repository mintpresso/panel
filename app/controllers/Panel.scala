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

object Panel extends Controller with Secured {
  def overview(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.overview())
  }
  def overview_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
  	Ok(views.html.panel._overview.index())
  }
  def overview_usage(accountId: Int) = SignedAccount(accountId) { implicit request =>
  	Ok(views.html.panel._overview.usage())
  }
  def overview_account(accountId: Int) = SignedAccount(accountId) { implicit request =>
  	Ok(views.html.panel._overview.account())
  }
  def overview_transaction(accountId: Int) = SignedAccount(accountId) { implicit request =>
  	Ok(views.html.panel._overview.transaction())
  }
  def overview_api(accountId: Int) = SignedAccount(accountId) { implicit request =>
  	Ok(views.html.panel._overview.api())
  }

  def data(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.data())
  }
  def data_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.index())
  }
  def data_view(accountId: Int, json: String) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.view())
  }
  def data_filter(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.filter())
  }
  def data_import(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.imports())
  }
  def data_export(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.exports())
  }
  def order(accountId: Int) = TODO
  def pickup(accountId: Int) = TODO
  def support(accountId: Int) = TODO
}