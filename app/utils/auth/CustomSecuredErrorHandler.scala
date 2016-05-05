package utils.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.Logger
import play.api.http.ContentTypes
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

/**
  * @author Vitor de Moraes
  */
class CustomSecuredErrorHandler @Inject()(val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Logger.debug("[Silhouette] Unauthenticated user trying to access '%s'".format(request.uri))
    produceResponse(Results.Unauthorized, Messages("silhouette.not.authenticated"))
  }

  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Logger.debug("[Silhouette] Unauthorized user trying to access '%s'".format(request.uri))
    produceResponse(Results.Forbidden, Messages("silhouette.not.authorized"))
  }

  private def produceResponse(status: Results.Status, message: String) =
    Future.successful(status(Json.obj("success" -> false, "message" -> message)).as(ContentTypes.JSON))

}
