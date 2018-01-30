package dao

import javax.inject.Inject

import models.{GroupTable, UserTable}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}

import scala.concurrent.{ExecutionContext, Future}


class PostgreStore @Inject()(
                              @NamedDatabase("channel_partners")
                              protected val dbConfigProvider: DatabaseConfigProvider)
                            (implicit val executionContext: ExecutionContext) extends
  HasDatabaseConfigProvider[JdbcProfile]{


  def addGroup(groupTable: GroupTable):Future[Int]={
    db.run(PostgreDbModels.groupTableRows.filter(_.groupName===groupTable.groupName).result.headOption).map{
      x=>
        if(!x.isDefined){
          db.run(PostgreDbModels.groupTableRows.insertOrUpdate(groupTable))
        }
        else{
          Future(0)
        }
    }.flatMap(c=>c)
  }

  def addChampion(userTable: UserTable):Future[Int]={
    db.run(PostgreDbModels.championTableRows.insertOrUpdate(userTable))
  }

  def getChampion(groupId:String,userId:String):Future[Option[UserTable]]={
    db.run(PostgreDbModels.championTableRows.filter(x=> x.groupId === groupId && x.userId === userId).result.headOption)
  }

  def addMember(userTable: UserTable):Future[Int]={
    db.run(PostgreDbModels.memberTableRows.insertOrUpdate(userTable))
  }

  def deleteMember(userId: String):Future[Int]={
    db.run(PostgreDbModels.memberTableRows.filter(_.userId===userId).delete)
  }

  def deleteChampion(userId: String):Future[Int]={
    db.run(PostgreDbModels.championTableRows.filter(_.userId===userId).delete)
  }




  object PostgreDbModels{

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

  }
}
