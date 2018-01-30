package models

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

import scala.beans.BeanProperty

/**
  * Created by saikrishna on 29/1/18.
  */
case class FireBaseUser(
                         id: String,
                         name: String,
                         email: String,
                         mobile: String,
                         permissions: Option[String],
                         groupIds: Option[String],
                         created_at: LocalDateTime = LocalDateTime.now(),
                         updated_at: LocalDateTime = LocalDateTime.now()
                       )


import scala.collection.JavaConverters._

case class Role(override val name: String) extends be.objectify.deadbolt.scala.models.Role {
  def toBean: RoleBean = {
    val r = new RoleBean()
    r.name = name
    r
  }
}

case class Permission(override val value: String) extends be.objectify.deadbolt.scala.models.Permission {
  def toBean: PermissionBean = {
    val p = new PermissionBean()
    p.value = value
    p
  }
}

case class User(uid: String, uuid: String, roles: List[Role], permissions: List[Permission]) {
  
  def toBean: UserBean = {
    val u = new UserBean()
    u.uid = uid
    u.uuid = uuid
    u.roles = roles.map(_.toBean).asJava
    u.permissions = permissions.map(_.toBean).asJava
    u
  }
}

case class UserG(uid: String, uuid: String,groups : List[String], roles: List[Role], permissions: List[Permission]) {
  
  def toBean: UserGBean = {
    val u = new UserGBean()
    u.uid = uid
    u.uuid = uuid
    u.groups = groups.asJava
    u.roles = roles.map(_.toBean).asJava
    u.permissions = permissions.map(_.toBean).asJava
    u
  }
}

case class Group (groupId : String ,gname : String , uuids : List[String], roles : List[Role] , permissions : List[Permission]) {
  def toBean : GroupBean = {
    val u = new GroupBean()
    u.gid = groupId
    u.gname = gname
    u.uuids = uuids.asJava
    u.roles = roles.map(_.toBean).asJava
    u.permissions = permissions.map(_.toBean).asJava
    u
  }
}

class RoleBean() {
  @BeanProperty var name: String = _
  
  def toCase: Role = {
    Role(name)
  }
}

class PermissionBean() {
  @BeanProperty var value: String = _
  
  def toCase: Permission = {
    Permission(value)
  }
}


class UserBean() {
  @BeanProperty var uid: String = _
  @BeanProperty var uuid: String = _
  @BeanProperty var roles: java.util.List[RoleBean] = List[RoleBean]().asJava
  @BeanProperty var permissions: java.util.List[PermissionBean] = List[PermissionBean]().asJava
  
  def toCase: User = {
    User(uid, uuid, roles.asScala.toList.map(_.toCase), permissions.asScala.toList.map(_.toCase))
  }
}

class UserGBean() {
  @BeanProperty var uid: String = _
  @BeanProperty var uuid: String = _
  @BeanProperty var groups : java.util.List[String] = List[String]().asJava
  @BeanProperty var roles: java.util.List[RoleBean] = List[RoleBean]().asJava
  @BeanProperty var permissions: java.util.List[PermissionBean] = List[PermissionBean]().asJava
  
  def toCase: UserG = {
    UserG(uid, uuid,groups.asScala.toList, roles.asScala.toList.map(_.toCase), permissions.asScala.toList.map(_.toCase))
  }
}

class GroupBean() {
  @BeanProperty var gid: String = _
  @BeanProperty var gname : String = _
  @BeanProperty var uuids: java.util.List[String] = List[String]().asJava
  @BeanProperty var roles: java.util.List[RoleBean] = List[RoleBean]().asJava
  @BeanProperty var permissions: java.util.List[PermissionBean] = List[PermissionBean]().asJava
  
  def toCase: Group = {
    Group(gid, gname , uuids.asScala.toList, roles.asScala.toList.map(_.toCase), permissions.asScala.toList.map(_.toCase))
  }
}

case class Subject(override val identifier: String, override val roles: List[Role], override val permissions: List[Permission]) extends be.objectify.deadbolt.scala.models.Subject



object userFormats {
  implicit val fireBaseUserFmt: OFormat[FireBaseUser] = Json.format[FireBaseUser]
  implicit val roleFmt: OFormat[Role] = Json.format[Role]
  implicit val permissionFmt: OFormat[Permission] = Json.format[Permission]
  implicit val UserFmt: OFormat[User] = Json.format[User]
  implicit val UserGFmt: OFormat[UserG] = Json.format[UserG]
  implicit val GroupFmt: OFormat[Group] = Json.format[Group]
}
