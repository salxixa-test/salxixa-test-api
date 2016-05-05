package controllers

import javax.inject._

import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import play.api.libs.json.Json
import play.api.mvc._
import utils.auth.DefaultEnv

import scala.concurrent.Future

@Singleton
class HomeController @Inject()(silhouette: Silhouette[DefaultEnv]) extends Controller {

  def index = Action {
    Ok(Json.toJson("OK"))
  }

  def user = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

  def signOut = silhouette.SecuredAction.async { implicit request =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }

}
