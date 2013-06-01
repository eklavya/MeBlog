package models

import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import Database.threadLocalSession
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import java.sql.Date

case class Blog(id: Option[Int], authorId: Int, title: String, content: String, createdOn: String)

object Blogs extends Table[Blog]("BLOGS") {
	def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
	def authorId = column[Int]("USER_FK")
	def title = column[String]("TITLE")
	def content = column[String]("CONTENT", O.DBType("varchar(10000)"))
	def createdOn = column[String]("CREATED_ON")
	def * = id.? ~ authorId ~ title ~ content ~ createdOn <> (Blog, Blog.unapply _)
	def autoInc = id.? ~ authorId ~ title ~ content ~ createdOn <> (Blog, Blog.unapply _) returning id

	lazy val db = Database.forDataSource(DB.getDataSource())

	def create(blog: Blog) = {
		// db withSession {
			Blogs.autoInc.insert(blog)
		// }
	}

	def blogsOfUser(userId: Int) : List[Blog] = {
		// db withSession {
			Query(Blogs).filter(_.authorId === userId).list
		// }
	}

	val blogsPerPage = 4;

	def pageCount: Int = {
		// db withSession {
			//Query(Blogs).length.map(_) 
			(Blogs map { _.id.count}).first  / blogsPerPage
		// }
	}

	def blogsInPage(pageNum: Int): List[Blog] = {
		if (pageNum < 1 || (pageNum > pageCount)) {
			// db withSession {
				Query(Blogs).sortBy(_.createdOn.desc).take(blogsPerPage).list
			// }
		}
		else {
			// db withSession {
				Query(Blogs).sortBy(_.createdOn.desc).drop((pageNum - 1)*blogsPerPage).take(blogsPerPage).list
			// }
		}
	}

	def blogById(id: Int): Option[Blog] = {
		db withSession {
			Query(Blogs).filter(_.id === id).list.headOption
		}
	}
}
