package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import dao.PostgreStore
import models._
import models.Implicits._
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class GroupsController @Inject() (implicit val postGreStore: PostgreStore,
                                  implicit val ec: ExecutionContext)extends Controller{

  def createGroup:Action[JsValue]=Action.async(parse.json){
    request=>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),

        dto=>{

          val groupId=UUID.randomUUID().toString

          val group = GroupTable(
              groupId = groupId,
            groupName = (dto \ "groupName").as[String]
          )

          postGreStore.addGroup(group).map{
            x=>
              if(x>0) {
                val champion=UserTable(
                  groupId = groupId,
                  userId = (dto \ "createdBy").as[String]
                )
                postGreStore.addChampion(champion)
                Ok(Json.obj("code" -> 0, "msg" -> "Group created Successfully"))
              }
              else Ok(Json.obj("code"->1,"msg"->"Group already exists"))
          }
        }
      )
  }

  def addMembers(groupId:String):Action[JsValue]=Action.async(parse.json){
    request=>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),

        dto=>{

          postGreStore.getChampion(groupId,(dto \ "userId").as[String]).map {
            y=>
              if(y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val member = UserTable(
                      groupId=groupId,
                      userId = (x \ "userId").as[String]
                    )
                    postGreStore.addMember(member)
                }
              Ok(Json.obj("code"->0,"msg"->"success"))
          }
        }
      )
  }

  def addChampion(groupId:String):Action[JsValue]=Action.async(parse.json){
    request=>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),

        dto=>{

          postGreStore.getChampion(groupId,(dto \ "userId").as[String]).map {
            y=>
              if(y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val champion = UserTable(
                      groupId=groupId,
                      userId = (x \ "userId").as[String]
                    )
                    postGreStore.addChampion(champion)
                }
              Ok(Json.obj("code"->0,"msg"->"success"))
          }
        }
      )
  }

  def removeChampion(groupId:String):Action[JsValue]=Action.async(parse.json){
    request=>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),

        dto=>{

          postGreStore.getChampion(groupId,(dto \ "userId").as[String]).map {
            y=>
              if(y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val userId = (x\ "userId").as[String]
                    postGreStore.deleteChampion(userId)
                }
              Ok(Json.obj("code"->0,"msg"->"success"))
          }
        }
      )
  }

  def removeMember(groupId:String):Action[JsValue]=Action.async(parse.json){
    request=>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),

        dto=>{

          postGreStore.getChampion(groupId,(dto \ "userId").as[String]).map {
            y=>
              if(y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val userId = (x \ "userId").as[String]
                    postGreStore.deleteChampion(userId)
                }
              Ok(Json.obj("code"->0,"msg"->"success"))
          }
        }
      )
  }


}
