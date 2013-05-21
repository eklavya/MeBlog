package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints._
import java.util.Date

object BlogCtrl extends Controller with Auth {

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
			valid => {
				val user = Users.getByName(username)
				val blogId = Blogs.create(Blog(None, user.get.id.get, valid._1, valid._2, (new Date()).toString()))
				//Ok(views.html.blog(Blogs.blogById(blogId).get)(Some(username)))
				Redirect("/blog/" + blogId)
			}
		)
	}

	def showBlog(id: Int) = Action { implicit request =>
		Blogs.blogById(id).map(blog => Ok(views.html.blog(blog)(username(request)))).getOrElse(BadRequest("Blog not found."))
	}

	def showPage(num: Int) = Action {implicit request =>
		Ok(views.html.blogPage(Blogs.blogsInPage(num))(num)(username(request)))
	}

	def comment = isAuthenticated { username => implicit request => 
		commentForm.bindFromRequest.fold (
			formWithErrors => BadRequest("Invalid info."),
			valid => {
				val user = Users.getByName(username) 
				Comments.create(Comment(None, valid._2, user.get.id.get, valid._1, (new Date()).toString()))
				//Ok(views.html.blog(Blogs.blogById(valid._2).get)(Some(username)))
				Redirect("/blog/" + valid._2)
			}
		)
	}
}