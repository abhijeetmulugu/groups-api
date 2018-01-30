
package dao

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import models.FireBaseUser
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


/**
  * Created by saikrishna on 1/11/17.
  */

@Singleton
class UsersDataBase @Inject()(
                               @NamedDatabase("channel_partners")
                               implicit val dbConfigProvider: DatabaseConfigProvider,
                               implicit val executionContext: ExecutionContext) {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  
  import dbConfig._
  import profile.api._
  
  class FireBaseUsersTable(tag: Tag) extends Table[FireBaseUser](tag, "firebaseuser") {

    implicit val timestampAColumnType = MappedColumnType.base[LocalDateTime, Timestamp](
      { dt ⇒ java.sql.Timestamp.valueOf(dt) }, { ts ⇒ ts.toLocalDateTime }
    )
    
    def id = column[String]("id", O.PrimaryKey)

    def name = column[String]("name")

    def email = column[String]("email")

    def mobile = column[String]("mobile")
    
    def permissions = column[String]("permissions")
    
    def groupIds = column[String]("group_ids")

    def created_at = column[LocalDateTime]("created_at", O.Default[LocalDateTime](LocalDateTime.now()))

    def updated_at = column[LocalDateTime]("updated_at", O.Default[LocalDateTime](LocalDateTime.now()))

    def * = (id, name, email, mobile , permissions.?, groupIds.? , created_at, updated_at) <> (FireBaseUser.tupled, FireBaseUser.unapply)
  }
  
  
  val fireBaseUserRows = TableQuery[FireBaseUsersTable]
  
  try {
    Logger.info(s"db.source.maxConnections ${db.source.maxConnections}")
    val tables = List(fireBaseUserRows)
    val existing = db.run(MTable.getTables)
    val f = existing.flatMap(v => {
      val names = v.map(mt => mt.name.name)
      val createIfNotExist = tables.filter(table => !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
      db.run(DBIO.sequence(createIfNotExist))
    })
    Await.result(f, Duration.Inf)
  } catch {
    case e: Exception ⇒ Logger.error("error while creating database tables", e)
  }

  
  def getUserById (id : String): Future[Option[FireBaseUser]] = {
    val query = fireBaseUserRows.filter(_.id === id)
    db.run(query.result.headOption)
  }
  
  def insertUser(user : FireBaseUser):Future[Int] = {
    try {
      Logger.debug("user inserted")
      db.run(fireBaseUserRows.insertOrUpdate(user))
    }
    catch{
      case ex:Exception=>
        println(s"$ex")
        Future(0)
    }
  }

}


