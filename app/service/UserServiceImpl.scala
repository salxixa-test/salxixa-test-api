package service

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import dao.UserDAO
import model.User

import scala.concurrent.Future

/**
  * @author Vitor de Moraes
  */
class UserServiceImpl @Inject()(userDAO: UserDAO) extends UserService {

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  override def save(user: User): Future[User] = userDAO.save(user)

}
