package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.data.validation.Constraints._
import java.util.Date

object BlogController extends Controller {

	val blogForm = Form (
		tuple (
			"title" -> nonEmptyText,
			"content" -> nonEmptyText
		)
	)

	val commentForm = Form (
		tuple (
			"comment" -> nonEmptyText,
			"blogId" -> number
		)
	)

	def newBlog = Action { implicit request =>
		blogForm.bindFromRequest.fold(
			formWithErrors => BadRequest("Please submit a proper blog."),
			valid =>  {
				val user = request.cookies.get("user")
				user.map{ z => 
					val name = z.value
					Users.getByName(name) match {
						case Some(x) => {
							val blogId = Blogs.create(Blog(None, x.id.get, valid._1, valid._2, (new Date()).toString())) //returning Blogs.id
							Ok(views.html.blog(Blogs.blogById(blogId).get))
						}
						case None => BadRequest("This  blog does not exist in the database !!")
					}
				}.getOrElse(Ok(views.html.login()))
			}
		)
	}

	def showBlog(id: Int) = Action { implicit request =>
		Blogs.blogById(id).map(blog => Ok(views.html.blog(blog))).getOrElse(BadRequest("Blog not found."))
	}

	def showPage(num: Int) = Action {implicit request =>
		Ok(views.html.blogPage(Blogs.blogsInPage(num))(num))
	}

	def comment = Action { implicit request => 
		commentForm.bindFromRequest.fold (
			formWithErrors => BadRequest("Invalid info."),
			valid => {
				val user = request.cookies.get("user")
				user.map{ z =>
					val name = z.value
					Users.getByName(name) match {
						case Some(x) => {
							Comments.create(Comment(None, valid._2, x.id.get, valid._1, (new Date()).toString())) //returning Blogs.id
							Ok(views.html.blog(Blogs.blogById(valid._2).get))
						}
						case None => BadRequest("To be able to post, you must be logged in.")
					}
				}.getOrElse(Ok(views.html.login()))
			}
		)
	}
}