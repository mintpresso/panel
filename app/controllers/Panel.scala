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
    Async {
      MintpressoCore.getToken(accountId).map { res =>
        res.status match {
          case 200 =>
            Ok(views.html.panel._overview.api(getUser, res.body))
          case 404 =>
            NotFound
          //case 403 =>
          case _ =>
            Forbidden
        }
      }
    }
  }
  def overview_api_set(accountId: Int) = SignedAccount(accountId) { implicit request =>
    val f = Form(
      tuple(
        "data" -> text,
        "password" -> text
      )
    )
    val form = f.bindFromRequest
    if(form.hasErrors){
      Ok.flashing("error" -> Messages("overview.api.fill"), "msg" -> "")
    }else{
      form.get match {
        case (domain: String, password: String) => {
          val url: Array[String] = domain.trim.split('\n')
          if(url.length == 0){
            Ok.flashing("error" -> Messages("overview.api.domain.fill"), "msg" -> "")  
          }else{
            Async {
              MintpressoCore.setToken(accountId, password, url.toList).map { response => 
                response.status match {
                  case 200 => {
                    Ok.flashing(
                        "created" -> (System.currentTimeMillis).toString,
                        "msg" -> Messages("overview.api.domain.updated")
                      )
                  }
                  case 403 => {
                    Ok.flashing("error" -> Messages("overview.api.password.invalid"), "domain" -> domain)
                  }
                  case _ => {
                    Ok.flashing("error" -> Messages("overview.api.retry"), "domain" -> domain)
                  }
                }
              }
            }
          }
        }
        case _ => Ok.flashing("error" -> Messages("overview.api.domain.fill"), "msg" -> "")
      }
    }
  }

  def data(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.data())
  }
  def data_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.index(""))
  }
  def data_view(accountId: Int, json: String) = SignedAccount(accountId) { implicit request =>
    var types: String = "[]"
    var points: String = "{\"points\": []}"
    var edges: String = "{\"edges\": []}"
    if(json.length == 0){
      Async {
        MintpressoAPI("user", accountId).getPointTypes().map { res =>
          res.status match {
            case 200 =>
              types = res.body
          }
        }
        MintpressoAPI("user", accountId).getLatestPoints().map { res =>
          res.status match {
            case 200 =>
              points = res.body
          }
          Ok(views.html.panel._data.view(types, points, edges, Map()))
        }
      }
    }else{
      val obj = Json.parse(json)

      val form = ((obj \ "s").asOpt[String], (obj \ "v").asOpt[String], (obj \ "o").asOpt[String])
      form match {
        case (s: Option[String], v: Option[String], o: Option[String]) => {
          val _s = s.getOrElse("")
          val _v = v.getOrElse("")
          val _o = o.getOrElse("")
          var values: Map[String, String] = Map(("s" -> _s), ("v" -> _v), ("o" -> _o))
          if( (_s.length + _v.length + _o.length) == 0 ){
            Async {
              MintpressoAPI("user", accountId).getPointTypes().map { res =>
                res.status match {
                  case 200 => {
                    types = res.body
                  }
                  case _ => {
                    println("get p t" + res.status)
                  }
                }
              }
              MintpressoAPI("user", accountId).getLatestPoints().map { res =>
                res.status match {
                  case 200 => {
                    points = res.body
                  }
                  case _ => {
                    println("g l p " + res.status)
                  }
                }
                Ok(views.html.panel._data.view(types, points, edges, values))
              }
            }
          }else{
            var query: Map[String, String] = Map()
            val Number = "([0-9]+)".r
            _s match {
              case Number(_s) => {
                query += (("subjectId", _s))
              }
              case _ => {
                query += (("subjectType", _s))
              }
            }
            _o match {
              case Number(_o) => {
                query += (("objectId", _o))
              }
              case _ => {
                query += (("objectType", _o))
              }
            }
            if(_v.length > 0){
              query += (("verb", _v))
            }

            val _before = System.currentTimeMillis

            Async {
              MintpressoAPI("user", accountId).getPointTypes().map { res =>
                res.status match {
                  case 200 => {
                    types = res.body
                  }
                  case _ => {
                    println("g p t 2" + res.status)
                  }
                }
              }
              MintpressoAPI("user", accountId).findRelations(query).map { res =>
                val _after = System.currentTimeMillis
                res.status match {
                  case 200 => {
                    values += (("msg" -> (" - <time>(" + ((_after - _before)/1000.0).toString +" seconds)</time>")))
                    Ok(views.html.panel._data.view(types, points, edges, values))
                  }
                  case _ => {
                    values += (("error" -> ((res.json \ "status" \ "message").as[String] + " - <time>(" + ((_after - _before)/1000.0).toString +" seconds)</time>")))
                    Ok(views.html.panel._data.view(types, points, edges, values))
                  }
                }
              }
            }
          }
        }
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
      Ok.flashing("error" -> Messages("data.import.fill"), "msg" -> "")
    }else{
      form.get match {
        case (model: String, identifier: Option[String], data: Option[String]) => {
          val _i = identifier.getOrElse("")
          val _d = data.getOrElse("")
          if(!MintpressoCore.Type.contains(model)){
            Ok.flashing("error" -> Messages("data.import.model"), "msg" -> "", "identifier" -> _i, "data" -> _d)
          }else{
            Async {
              MintpressoAPI("user", accountId).addPoint(model, _i, _d).map { response => 
                response.status match {
                  case 200 => {
                    Ok.flashing(
                        "created" -> (System.currentTimeMillis).toString,
                        "msg" -> Messages("data.import.notCreated"),
                        "model" -> model, "identifier" -> _i, "data" -> _d
                      )
                  }
                  case 201 => {
                    //(response.json \ "account").asOpt[JsObject].map { obj =>
                    Ok.flashing(
                        "created" -> (System.currentTimeMillis).toString,
                        "msg" -> (Messages("data.import.created") + Messages((response.json \ "status" \ "message").asOpt[String].getOrElse(""))),
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