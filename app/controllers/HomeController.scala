package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

class HomeController @Inject()(silhouette: Silhouette[DefaultEnv]) extends Controller {

  def index = Action {
    Ok(Json.toJson("OK"))
  }

  def user = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

}
