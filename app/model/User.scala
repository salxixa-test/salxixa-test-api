package model

import java.util.UUID

import com.mohiva.play.silhouette.api.{AuthInfo, Identity, LoginInfo}
import play.api.libs.json.Json

/**
  * @author Vitor de Moraes
  */
case class User(
  userID: UUID,
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarURL: Option[String]) extends Identity with AuthInfo

object User {

  implicit val jsonFormat = Json.format[User]

}
