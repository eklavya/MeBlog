package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints._
import java.util.Date
import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import Database.threadLocalSession
import play.api.Play.current

object Application extends Controller with Auth {

  lazy val db = Database.forDataSource(DB.getDataSource())

  val loginForm = Form (
    tuple (
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    ) verifying( "Authentication failed.", fields => fields match {
        case(user, pass) => db withSession {
          Users.auth(user, pass)
        }
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
    Redirect("/Page").withNewSession
  }

  def home = isAuthenticated { username => implicit request =>
    Ok(views.html.home(username)(db withSession {Blogs.blogsOfUser(Users.getByName(username).get.id.get)}))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold (
      formWithErrors => BadRequest( "Invalid credentials!" ),
      valid => Redirect("/Page").withSession("user" -> valid._1)
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
              if (db withSession { Users exists user.email }) {
                BadRequest("User already exists. Please login using your exisitng username.")
              }
              else {
                db withSession {
                  Users.create(user)
                }
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