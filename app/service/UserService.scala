package service

import com.mohiva.play.silhouette.api.services.IdentityService
import model.User

import scala.concurrent.Future

/**
  * @author Vitor de Moraes
  */
trait UserService extends IdentityService[User] {

  /**
    * Saves a user.
    *
    * @param user The user to save.
    *
    * @return The saved user.
    */
  def save(user: User): Future[User]

}
