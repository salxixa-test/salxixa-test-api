package forms

import play.api.data.Form
import play.api.data.Forms._

/**
  * @author Vitor de Moraes
  */
object SignInForm {

  val form = Form(
    mapping(
      "email" -> email,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    email: String,
    password: String,
    rememberMe: Boolean
  )

}
