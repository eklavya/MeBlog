package models

import scala.slick.driver.MySQLDriver.simple._
import play.api.db.DB
import Database.threadLocalSession
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._


case class User(id: Option[Int], name: String, email: String, password: String)

object Users extends Table[User] ("USERS") {
	def id       = column[Int]("ID", O.PrimaryKey, O.AutoInc)
	def name     = column[String]("NAME")
	def email    = column[String]("EMAIL")
	def password = column[String]("PASSWORD")
	def *        = id.? ~ name ~ email ~ password <> (User, User.unapply _)

	lazy val db = Database.forDataSource(DB.getDataSource())

	def auth(user: String, pass: String) = {
		(for (q <- Users if (q.name === user) && (q.password === pass)) yield true).list.headOption.getOrElse(false)
	}

	def create(user: User) {
		Users.insert(User(None, user.name, user.email, user.password))
	}

	def exists(email: String) : Boolean = {
		(for (q <- Users if (q.email === email)) yield true).list.headOption.getOrElse(false)
	}

	def getByName(name: String): Option[User] = {
		(for (q <- Users if (q.name === name)) yield(q)).list.headOption
	}

	def nameById(id: Int): String = {
		Query(Users).filter(_.id === id).list.head.name
	}
}

