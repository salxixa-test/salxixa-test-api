package dao

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

case class PersistentPasswordInfo(loginInfo: LoginInfo, authInfo: PasswordInfo)

class PasswordInfoDAO @Inject()(reactiveMongoApi: ReactiveMongoApi) extends DelegableAuthInfoDAO[PasswordInfo] {

  implicit val passwordInfoFormat = Json.format[PasswordInfo]
  implicit val persistentPasswordInfoFormat = Json.format[PersistentPasswordInfo]

  def collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("password")

  override def find(loginInfo: LoginInfo) = {

    val passwordInfo: Future[Option[PersistentPasswordInfo]] = collection
      .find(Json.obj("loginInfo" -> loginInfo))
      .one[PersistentPasswordInfo]

    passwordInfo.flatMap {
      case None => Future.successful(Option.empty[PasswordInfo])
      case Some(persistentPasswordInfo) => Future(Some(persistentPasswordInfo.authInfo))
    }

  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    Future.successful(authInfo)
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    collection.insert(PersistentPasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    Future.successful(())
  }

}
