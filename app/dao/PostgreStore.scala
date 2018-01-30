package dao

import javax.inject.Inject

import models.{GroupTable, UserTable}
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class PostgreStore @Inject()(
                              @NamedDatabase("channel_partners")
                              protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit val executionContext: ExecutionContext){

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class groupInfo(tag: Tag) extends Table[GroupTable](tag, "GROUPS_INFO") {

    def groupId = column[String]("groupId",O.PrimaryKey)

    def groupName = column[String]("groupName")

    def * = (groupId, groupName) <> (GroupTable.tupled, GroupTable.unapply)
  }

  class championInfo(tag: Tag) extends Table[UserTable](tag, "CHAMPION_INFO") {

    def id =column[Int]("id",O.PrimaryKey,O.AutoInc)

    def groupId = column[String]("groupId")

    def userId = column[String]("userId")


    def * = (id,groupId, userId) <> (UserTable.tupled, UserTable.unapply)
  }
  class memberInfo(tag: Tag) extends Table[UserTable](tag, "MEMBER_INFO") {

    def id =column[Int]("id",O.PrimaryKey,O.AutoInc)

    def groupId = column[String]("groupId")

    def userId = column[String]("userId")


    def * = (id,groupId, userId) <> (UserTable.tupled, UserTable.unapply)
  }

  val groupTableRows: TableQuery[groupInfo] = TableQuery[groupInfo]
  val championTableRows: TableQuery[championInfo] = TableQuery[championInfo]
  val memberTableRows: TableQuery[memberInfo] = TableQuery[memberInfo]


  def addGroup(groupTable: GroupTable):Future[Int]={
    db.run(groupTableRows.filter(_.groupName===groupTable.groupName).result.headOption).map{
      x=>
        if(!x.isDefined){
          db.run(groupTableRows.insertOrUpdate(groupTable))
        }
        else{
          Future(0)
        }
    }.flatMap(c=>c)
  }

  def addChampion(userTable: UserTable):Future[Int]={
    db.run(championTableRows.insertOrUpdate(userTable))
  }

  def getChampion(groupId:String,userId:String):Future[Option[UserTable]]={
    db.run(championTableRows.filter(x=> x.groupId === groupId && x.userId === userId).result.headOption)
  }

  def addMember(userTable: UserTable):Future[Int]={
    try {
      Logger.debug(s"member updated-->$userTable")
      db.run(memberTableRows.insertOrUpdate(userTable))
    }
    catch{
      case ex: Exception =>
        Logger.debug(s"ex->$ex")
       Future(0)
    }

  }

  def deleteMember(userId: String):Future[Int]={
    db.run(memberTableRows.filter(_.userId===userId).delete)
  }

  def deleteChampion(userId: String):Future[Int]={
    db.run(championTableRows.filter(_.userId===userId).delete)
  }

  try {
    Logger.info(s"db.source.maxConnections ${db.source.maxConnections}")
    val tables = List(groupTableRows,championTableRows,memberTableRows)
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




}
