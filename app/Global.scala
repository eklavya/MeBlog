import play.api.db.DB
import play.api.GlobalSettings
// Use H2Driver to connect to an H2 database
import scala.slick.driver.H2Driver.simple._
 
// Use the implicit threadLocalSession
import Database.threadLocalSession
import scala.slick.jdbc.meta._
import play.api.Application
import play.api.Play.current
import models._
 
object Global extends GlobalSettings {
	
	lazy val db = Database.forDataSource(DB.getDataSource())
	
	override def onStart(app: Application) {
		db withSession {
			if (MTable.getTables("USERS").list.isEmpty)
				Users.ddl.create
			if (MTable.getTables("BLOGS").list.isEmpty)
				Blogs.ddl.create
			if (MTable.getTables("COMMENTS").list.isEmpty)
				Comments.ddl.create
		}	
	}
}