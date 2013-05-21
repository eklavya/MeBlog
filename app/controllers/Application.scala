package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms._
import play.api.data._
import models._

object Application extends Controller with Auth {

  val loginForm = Form (
    tuple (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying( "Authentication failed.", fields => fields match {
        case(user, pass) => Users.auth(user, pass)
      })
  ) 

  val userForm = Form (
  mapping (
  	"id" -> ignored(Option(1)),
  	"name" -> nonEmptyText,
  	"email" -> nonEmptyText,
  		"password" -> nonEmptyText,
  		"rePassword" -> nonEmptyText
  	) ((id, name, email, password, _) => User(None, name, email, password))(user => Some(None, user.name, user.email, user.password, ""))				
  )

  def logout = Action { implicit request => 
    //Ok(views.html.blogPage(Blogs.blogsInPage(1))(1)(username(request)))
    Redirect("/Page").withNewSession
  }

  def home = isAuthenticated { username => implicit request =>
    Ok(views.html.home(username))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold (
      formWithErrors => BadRequest( "Invalid credentials!" ),
      valid => 
          Ok(views.html.blogPage(Blogs.blogsInPage(1))(1)(Some(valid._1))).withSession("user" -> valid._1)
    )
  }

  def register = Action { implicit request =>
    if (request.method.equals("GET")) {
      Ok(views.html.register())
    }
    else {
      userForm.bindFromRequest.fold(
        formWithErrors => BadRequest( "Invalid information." ),
        user => {
            if (Users exists user.email) {
              BadRequest("User already exists. Please login using your exisitng username.")
            }
            else {
              Users.create(user)
              Redirect("/home").withSession("user" -> user.name)
            }
          }
        )
    }
  }

}

trait Auth {

	def username(request: RequestHeader) = request.session.get("user")

	def unauthorized(request: RequestHeader) = Results.BadRequest("You are not authorized to perform this action. Please login and try again.")

	def isAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, unauthorized) { user =>
	    Action(request => f(user)(request))
	}
}