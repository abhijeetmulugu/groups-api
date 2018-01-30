package models

import play.api.libs.json.{JsValue, Json, OFormat}

case class GroupRequest(
                       groupName :String,
                       userId:String,
                       groupDetails:JsValue
                       )

case class GroupTable(
                     groupId:String,
                     groupName:String
                     )

case class UserTable(
                          id:Int=0,
                          groupId:String,
                          userId:String
                        )
case class URL_shortened(
                          id: String,
                          longUrl: String
                        )

object Implicits {
  implicit val GroupRequestFmt: OFormat[GroupRequest] = Json.format[GroupRequest]
  implicit val GroupTableFmt: OFormat[GroupTable] = Json.format[GroupTable]
  implicit val MembersTableFmt: OFormat[UserTable] = Json.format[UserTable]
  implicit val URL_shortenedFmt: OFormat[URL_shortened] = Json.format[URL_shortened]

}
