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

object Panel extends Controller with Secured {
  val mintpresso: Affogato = Affogato( Play.configuration.getString("mintpresso.internal.api").getOrElse(""), Play.configuration.getString("mintpresso.internal.id").getOrElse("0").toLong)

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
    Ok(views.html.panel._overview.api(getUser, User.findTokens(accountId, getUser.email).toString))
  }
  def overview_api_set(accountId: Int, key: String, domain: String, name: String) = SignedAccount(accountId) { implicit request =>    
    import java.net.{InetAddress, URL, MalformedURLException, UnknownHostException}
    try {
      if(key.length == 0){
        throw new Exception(Messages("overview.api.key.fill"))
      }
      if(domain.length == 0){
        throw new Exception(Messages("overview.api.domain.fill"))
      }
      if(name.length == 0){
        throw new Exception(Messages("overview.api.name.fill"))
      }
      val url: Array[String] = domain.trim.split('\n')
      if(url.length == 0){
        throw new Exception(Messages("overview.api.domain.fill"))
      }
      var address: Array[String] = Array()
      
      // fill the form

      // query ip address
      url.foreach { item =>
        if(item != "*"){
          var query: InetAddress = InetAddress.getByName(new URL("http://"+item).getHost())
          var ip = query.getHostAddress()
          // add to address list
          address :+= (ip.toString)
        }
      }

      var updateToken = MintpressoAPI("internal").addPoint("token", key, Json.obj(
        "name" -> name,
        "expired" -> false,
        "url" -> url.mkString("|"),
        "address" -> address.mkString("|")
      ).toString, true)

      import scala.concurrent._
      import scala.concurrent.duration._
      var res1 = Await.result(updateToken, Duration(2000, MILLISECONDS))
      
      res1.status match {
        case 200 =>
          Ok.flashing("error" -> "", "msg" -> Messages("none.changes"))
        case 201 =>
          Ok.flashing("error" -> "", "msg" -> Messages("overview.api.done"))
        case _ =>
          Ok.flashing("error" -> Messages("overview.api.retry"), "domain" -> domain)
      }
    } catch {
      case e: MalformedURLException =>
        Ok.flashing("error" -> Messages("overview.api.domain.url", e.getMessage), "msg" -> "", "domain" -> domain)
      case e: UnknownHostException =>
        Ok.flashing("error" -> Messages("overview.api.domain.host", e.getMessage), "msg" -> "", "domain" -> domain)
      case e: Exception =>
        Ok.flashing("error" -> e.getMessage, "msg" -> "", "domain" -> domain)  
    }
  }

  def data(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.data())
  }
  def data_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.index())
  }

  def data_view(accountId: Int, json: String) = SignedAccount(accountId) { implicit request =>
    var types: String = "[]"
    var points: String = "{\"points\": []}"
    var edges: String = "{\"edges\": []}"
    if(json.length == 0){
      Async {
        MintpressoAPI("user", accountId, "").getPointTypes().map { res =>
          res.status match {
            case 200 =>
              types = res.body
            case _ => {
              Logger.warn(res.status + " at getPointTypes")
            }
          }
        }
        MintpressoAPI("user", accountId, "").getLatestPoints().map { res =>
          res.status match {
            case 200 =>
              points = res.body
            case _ => {
              println(res.body)
              Logger.warn(res.status + " at getLatestPoints")
            }
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
              MintpressoAPI("user", accountId, "").getPointTypes().map { res =>
                res.status match {
                  case 200 => {
                    types = res.body
                  }
                  case _ => {
                    println("get p t" + res.status)
                  }
                }
              }
              MintpressoAPI("user", accountId, "").getLatestPoints().map { res =>
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
              MintpressoAPI("user", accountId, "").getPointTypes().map { res =>
                res.status match {
                  case 200 => {
                    types = res.body
                  }
                  case _ => {
                    println("g p t 2" + res.status)
                  }
                }
              }
              MintpressoAPI("user", accountId, "").findRelations(query, true).map { res =>
                val _after = System.currentTimeMillis
                res.status match {
                  case 200 => {
                    edges = res.body
                    values += (("msg" -> ((res.json \ "_length").as[Int] + " results - <time>(" + ((_after - _before)/1000.0).toString +" seconds)</time>")))
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

  def data_log(accountId: Int) = SignedAccount(accountId) { implicit request =>
    var warnings = "{_length: 0}"
    var requests = "{_length: 0}"
    var errors = "{_length: 0}"
    var getWarnings = MintpressoAPI("user", accountId).findRelations(Map(
      "subjectId" -> accountId.toString,
      "verb" -> "log",
      "objectType" -> "warning"
    ), true)
    var getRequests = MintpressoAPI("user", accountId).findRelations(Map(
      "subjectId" -> accountId.toString,
      "verb" -> "log",
      "objectType" -> "request"
    ), true)
    var getErrors = MintpressoAPI("user", accountId).findRelations(Map(
      "subjectId" -> accountId.toString,
      "verb" -> "log",
      "objectType" -> "error"
    ), true)

    import scala.concurrent._
    import scala.concurrent.duration._
    var res1 = Await.result(getWarnings, Duration(2000, MILLISECONDS))
    var res2 = Await.result(getRequests, Duration(2000, MILLISECONDS))
    var res3 = Await.result(getErrors, Duration(2000, MILLISECONDS))
    
    res1.status match {
      case 200 =>
        warnings = res1.body
      case _ =>
        Logger.warn(res1.status + " at findRelations")
    }
    res2.status match {
      case 200 =>
        requests = res2.body
      case _ =>
        Logger.warn(res2.status + " at findRelations")
    }
    res3.status match {
      case 200 =>
        errors = res3.body
      case _ =>
        Logger.warn(res3.status + " at findRelations")
    }
    Ok(views.html.panel._data.log(errors, warnings, requests) )
  }
  def data_filter(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.filter())
  }
  def data_import(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Async {
      MintpressoAPI("user", accountId, "").getPointTypes().map { res =>
        res.status match {
          case 200 => {
            Ok(views.html.panel._data.imports( getUser, res.body))
          }
          case _ => {
            Ok(views.html.panel._data.imports( getUser, "[]"))
          }
        }
      }
    }
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
          Async {
            MintpressoAPI("user", accountId, "").addPoint(model, _i, _d).map { response => 
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
        case _ => Ok.flashing("error" -> Messages("data.import.fill"), "msg" -> "")
      }
    }
  }
  def data_export(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._data.exports())
  }
  def order(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.order())
  }
  def order_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._order.index())
  }
  def pickup(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.pickup())
  }
  def pickup_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._pickup.index())
  }
  def support(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel.support())
  }
  def support_index(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._support.index(getUser))
  }
  def support_conversation(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._support.conversation())
  }
  def support_consulting(accountId: Int) = SignedAccount(accountId) { implicit request =>
    Ok(views.html.panel._support.consulting())
  }
}