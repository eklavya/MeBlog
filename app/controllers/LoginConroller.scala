package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints._

object LoginController extends Controller {
  
  val loginForm = Form (
    tuple (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying( "Authentication failed.", fields => fields match {
        case(user, pass) => Users.auth(user, pass)
      })
  ) 

  def logout = Action {
    Ok("You have successfully logged out.").discardingCookies(DiscardingCookie("user"))
  }

  def home = Action { implicit request =>
    request.cookies.get("user").map(user => Ok(views.html.home(user.value))).getOrElse(Ok(views.html.login()))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold (
      formWithErrors => BadRequest( "Invalid credentials!" ),
      valid => 
          Ok(views.html.home(valid._1)).withCookies(Cookie("user", valid._1))
    )
  }

  def register = Action { implicit request =>
    if (request.method.equals("GET")) {
      Ok(views.html.register())
    }
    else {
      Users.userForm.bindFromRequest.fold(
        formWithErrors => BadRequest( "Invalid information." ),
        user => {
            if (Users exists user.email) {
              BadRequest("User already exists. Please login using your exisitng username.")
            }
            else {
              Users.create(user)
              Ok(views.html.home(user.name)).withCookies(Cookie("user", user.name))
            }
          }
        )
    }
  }

}