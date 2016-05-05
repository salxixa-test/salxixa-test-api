package dao

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import model.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
  * @author Vitor de Moraes
  */
class UserDAOImpl @Inject()(reactiveMongoApi: ReactiveMongoApi) extends UserDAO {

  def collection: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("user")

  /**
    * Finds a user by its login info.
    *
    * @param loginInfo The login info of the user to find.
    *
    * @return The found user or None if no user for the given login info could be found.
    */
  override def find(loginInfo: LoginInfo): Future[Option[User]] = {
    collection.find(Json.obj("loginInfo" -> loginInfo)).one[User]
  }

  /**
    * Finds a user by its user ID.
    *
    * @param userID The ID of the user to find.
    *
    * @return The found user or None if no user for the given ID could be found.
    */
  override def find(userID: UUID): Future[Option[User]] = {
    collection.find(Json.obj("userID" -> userID)).one[User]
  }

  /**
    * Saves a user.
    *
    * @param user The user to save.
    *
    * @return The saved user.
    */
  override def save(user: User): Future[User] = {
    collection.update(Json.obj("userId" -> user.userID),
      user,
      upsert = true
    )

    Future.successful(user)
  }

}
