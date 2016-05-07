package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.api.{LoginEvent, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import net.ceedubs.ficus.Ficus._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.api.{Configuration, Logger}
import service.UserService
import utils.auth.DefaultEnv

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class AuthController @Inject()(
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration,
  clock: Clock) extends Controller with I18nSupport {

  implicit val dataReads = (
    (__ \ 'email).read[String] and
      (__ \ 'password).read[String] and
      (__ \ 'rememberMe).read[Boolean]
    ) (SignInForm.Data.apply _)

  def login = Action.async(parse.json) { implicit request =>
    request.body.validate[SignInForm.Data].map { data =>
      credentialsProvider.authenticate(Credentials(data.email, data.password)).flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) => silhouette.env.authenticatorService.create(loginInfo).map {
            case authenticator if data.rememberMe =>
              val c = configuration.underlying
              authenticator.copy(
                expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
              )
            case authenticator => authenticator
          }.flatMap { authenticator =>
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            silhouette.env.authenticatorService.init(authenticator).map { token =>
              Ok(Json.obj("token" -> token))
            }
          }
          case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }.recover {
        case e: ProviderException => Unauthorized(Json.obj("message" -> Messages("invalid.credentials")))
      }
    }.recoverTotal {
      case error => Future.successful(Unauthorized(Json.obj("message" -> Messages("invalid.credentials"))))
    }
  }

  def logout = silhouette.SecuredAction.async { implicit request =>
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, Ok)
  }

  def isAuthenticated = silhouette.UserAwareAction { implicit request =>
    request.identity match {
      case Some(identity) => Ok
      case None => Unauthorized
    }
  }

}
