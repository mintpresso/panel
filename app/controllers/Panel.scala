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
    Async {
      MintpressoAPI("user", accountId).getLatestPoints().map { res =>
        res.status match {
          case 200 =>
            Ok(views.html.panel._data.index(res.body))
          case 404 =>
            Ok(views.html.panel._data.index(""))
          case _ =>
            InternalServerError
        }
      }
    }
  }
  def data_view(accountId: Int, json: String) = SignedAccount(accountId) { implicit request =>
    Async {
      MintpressoAPI("user", accountId).getPointTypes().map { res =>
        Ok(views.html.panel._data.view(res.body, "{\"points\": []}"))
      }
    }
  }
  def data_filter(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.filter())
  }
  def data_import(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.imports( getUser, MintpressoCore.Types))
  }
  def data_import_add(accountId: Int) = SignedAccount(accountId) { implicit request =>
    val f = Form(
      tuple(
        "model" -> text,
        "identifier" -> optional(text),
        "data" -> optional(text)
      )
    )
    val form = f.bindFromRequest
    if(form.hasErrors){
      Redirect(routes.Panel.data(accountId)).flashing("hash" -> "import", "error" -> Messages("data.import.fill"), "msg" -> "")
    }else{
      form.get match {
        case (model: String, identifier: Option[String], data: Option[String]) => {
          val _i = identifier.getOrElse("")
          val _d = data.getOrElse("")
          if(!MintpressoCore.Type.contains(model)){
            Ok.flashing("hash" -> "import", "error" -> Messages("data.import.model"), "msg" -> "", "identifier" -> _i, "data" -> _d)
          }else{
            Async {
              MintpressoAPI("user", accountId).addPoint(model, _i, _d).map { response => 
                response.status match {
                  case 200 => {
                    Ok.flashing(
                        "created" -> "zzz",
                        "msg" -> Messages("data.import.notCreated"),
                        "model" -> model, "identifier" -> _i, "data" -> _d
                      )
                  }
                  case 201 => {
                    //(response.json \ "account").asOpt[JsObject].map { obj =>
                    Ok.flashing(
                        "created" -> "zzz",
                        "msg" -> Messages("data.import.created"),
                        "model" -> model, "identifier" -> _i, "data" -> _d
                      )
                  }
                  case 500 => {
                    Ok.flashing("error" -> Messages("data.import.retry"), "msg" -> "", "model" -> model, "identifier" -> _i, "data" -> _d)
                  }
                  case _ => {
                    Ok.flashing("error" -> Messages("data.import.retry"), "msg" -> "", "model" -> model, "identifier" -> _i, "data" -> _d)
                  }
                }
              }
            }
          }
        }
        case _ => Ok.flashing("error" -> Messages("data.import.fill"), "msg" -> "")
      }
    }
  }
  def data_export(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.exports())
  }
  def order(accountId: Int) = TODO
  def pickup(accountId: Int) = TODO
  def support(accountId: Int) = TODO
}