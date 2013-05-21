package models

import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import Database.threadLocalSession
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import java.sql.Date

case class Comment(id: Option[Int], blogId: Int, userId: Int, content: String, dated: String)

object Comments extends Table[Comment] ("COMMENTS") {
	def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
	def blogId = column[Int]("BLOG_FK")//, id, Blogs) (_.id)
	def userId = column[Int]("USER_ID")//, id, Users) (_.id)
	def content = column[String]("CONTENT", O.DBType("varchar(512)"))
	def dated = column[String]("DATED")
	def * = id.? ~ blogId ~ userId ~ content ~ dated <> (Comment, Comment.unapply _)

	lazy val db = Database.forDataSource(DB.getDataSource())

	def create(comment: Comment) {
		db withSession {
			Comments.insert(comment)
		}
	}

	def commentsForBlog(blogId: Int): List[Comment] = {
		db withSession {
			Query(Comments).filter(_.blogId === blogId).list
		}
	}

	def commentsOfUser(userId: Int): List[Comment] = {
		db withSession {
			Query(Comments).filter(_.userId === userId).list
		}
	}
}