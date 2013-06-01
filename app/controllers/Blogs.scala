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

object BlogCtrl extends Controller with Auth {

	lazy val db = Database.forDataSource(DB.getDataSource())

	val blogForm = Form (
		tuple (
			"title" -> nonEmptyText,
			"content" -> nonEmptyText(maxLength = 10000)
		)
	)

	val commentForm = Form (
		tuple (
			"comment" -> nonEmptyText(maxLength = 512),
			"blogId" -> number
		)
	)

	def newBlog = isAuthenticated { username => implicit request =>
		blogForm.bindFromRequest.fold(
			formWithErrors => BadRequest("Please submit a proper blog."),
			valid => db withSession {
				val user = Users.getByName(username)
				val blogId = Blogs.create(Blog(None, user.get.id.get, valid._1, valid._2, (new Date()).toString()))
				Redirect("/blog/" + blogId)
			}
		)
	}

	def showBlog(id: Int) = Action { implicit request =>
		db withSession {
			Blogs.blogById(id) 
		}.fold {
			BadRequest(views.html.blogPage(Blogs.blogsInPage(1))(1)(username(request)))
			} { blog => Ok(views.html.blog(blog)(username(request)))}
	}

	def showPage(num: Int) = Action {implicit request =>
		db withSession {
			Ok(views.html.blogPage(Blogs.blogsInPage(num))(num)(username(request)))
		}
	}

	def comment = isAuthenticated { username => implicit request => 
		commentForm.bindFromRequest.fold (
			formWithErrors => BadRequest("Invalid info."),
			valid => {
				db withSession {
					val user = Users.getByName(username) 
					Comments.create(Comment(None, valid._2, user.get.id.get, valid._1, (new Date()).toString()))
				}
				Redirect("/blog/" + valid._2)
			}
		)
	}
}