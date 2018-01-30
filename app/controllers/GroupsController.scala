package controllers

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import javax.jws.WebService

import be.objectify.deadbolt.scala.DeadboltActions
import dao.{PostgreStore, UsersDataBase}
import models.{URL_shortened, _}
import models.Implicits._
import models.userFormats._
import play.api.{Configuration, Logger}
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api._

import scala.concurrent.{Await, ExecutionContext, Future}


@Singleton
class GroupsController @Inject()(implicit val postGreStore: PostgreStore,
                                 cc: ControllerComponents,
                                 deadbolt: DeadboltActions,
                                 usersDataBase: UsersDataBase,
                                 conf: Configuration,
                                 ws: WSClient,
                                 implicit val ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {
  
  private val ii_last_login_date = "ii_last_login"
  private val ii_auth_token = "Authorization"
  
  def index = Action {
    Ok("Group-Wrapper-Api")
  }

  def createTest = Action {
    val fireBaseUser = FireBaseUser(
      id = "",
      name = "",
      mobile = "",
      email = "",
      permissions = Some("dsada"),
      groupIds =Some("dsda")
    )
    usersDataBase.insertUser(fireBaseUser)
    Ok("argagsd")
  }

  def createGroup: Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),
        
        dto => {
          
          val groupId = UUID.randomUUID().toString
          
          val group = GroupTable(
            groupId = groupId,
            groupName = (dto \ "groupName").as[String]
          )
          
          postGreStore.addGroup(group).map {
            x =>
              if (x > 0) {
                val champion = UserTable(
                  groupId = groupId,
                  userId = (dto \ "createdBy_userId").as[String]
                )
                postGreStore.addChampion(champion)
                Logger.debug(s"champion-->$champion")
                ws.url(conf.get[String]("create-group")).post(dto).map(response=>response.json).map{
                  x=>
                    if((x \ "code").as[Int]==0)
                      Ok(Json.obj("code" -> 0, "msg" -> "Group created Successfully"))
                    else
                      Ok(Json.obj("code" -> 1, "msg" -> "Error"))

                }

              }
              else Future(Ok(Json.obj("code" -> 1, "msg" -> "Group already exists")))
          }.flatMap(c=>c)
        }
      )
  }
  
  def addMembers(groupId: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),
        
        dto => {
          
          postGreStore.getChampion(groupId, (dto \ "userId").as[String]).map {
            y =>
              if (y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val mobile = (x \ "mobile").as[String]
                    val email = (x \ "email").asOpt[String].getOrElse("")
                    val name = (x \ "fullName").as[String]
                    val id = UUID.randomUUID().toString
                    val member = UserTable(
                      groupId = groupId,
                      userId = id
                    )
                    
                    val fireBaseUser = FireBaseUser(
                      id = id,
                      name = name,
                      mobile = mobile,
                      email = email,
                      permissions = Some(""),
                      groupIds = Some("")
                    )
                    usersDataBase.insertUser(fireBaseUser).map(c=>Logger.debug(s"users->$c"))
                    val url = s"${conf.get[String]("app.auth.register.url")}?id=$id"
                    val payload = Json.obj(
                      "type" -> "jiva-user-register",
                      "application" -> "AUTHSERVER",
                      "payload" -> Json.obj(
                        "mobile" -> mobile,
                        "url" -> urlShortener(url)
                      )
                    )
                    Logger.debug(s"member->$member")
                    sendSMS(payload)
                    postGreStore.addMember(member).map(x=> Logger.debug(s"rows->$x"))
                }
              val s=Json.obj("members"->(dto \ "members").as[List[JsObject]])
              ws.url(conf.get[String]("update-members")+groupId).post(s).map(response=>response.json).map{
                x=>
                  if((x \ "code").as[Int]==0)
                    Ok(Json.obj("code" -> 0, "msg" -> "success"))
                  else
                    Ok(Json.obj("code" -> 1, "msg" -> "failure"))

              }

          }.flatMap(c=>c)
        }
      )
  }
  
  def addChampion(groupId: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),
        
        dto => {
          
          postGreStore.getChampion(groupId, (dto \ "userId").as[String]).map {
            y =>
              if (y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val champion = UserTable(
                      groupId = groupId,
                      userId = (x \ "userId").as[String]
                    )
                    postGreStore.addChampion(champion)
                }
              Ok(Json.obj("code" -> 0, "msg" -> "success"))
          }
        }
      )
  }
  
  def removeChampion(groupId: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),
        
        dto => {
          
          postGreStore.getChampion(groupId, (dto \ "userId").as[String]).map {
            y =>
              if (y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val userId = (x \ "userId").as[String]
                    postGreStore.deleteChampion(userId)
                }
              Ok(Json.obj("code" -> 0, "msg" -> "success"))
          }
        }
      )
  }
  
  def removeMember(groupId: String): Action[JsValue] = Action.async(parse.json) {
    request =>
      request.body.validate[JsObject].fold(
        errors => Future.successful(Ok(Json.obj("code" -> 1, "message" -> "INVALID_JSON", "errors" -> JsError.toJson(errors)))),
        
        dto => {
          
          postGreStore.getChampion(groupId, (dto \ "userId").as[String]).map {
            y =>
              if (y.isDefined)
                (dto \ "members").as[List[JsObject]].map {
                  x =>
                    val userId = (x \ "userId").as[String]
                    postGreStore.deleteChampion(userId)
                }
              Ok(Json.obj("code" -> 0, "msg" -> "success"))
          }
        }
      )
  }
  
  
  def insertUserInFireBase = deadbolt.SubjectPresent()() {
    implicit request ⇒
      val token = request.getQueryString("token").get
      val id = request.getQueryString("id").get
      val r = request.getQueryString("r").getOrElse("")
      Logger.info(s"redirection $r token $token")
      usersDataBase.getUserById(id).flatMap {
        case Some(usr) =>
          Logger.info(s"response from pgDB of firebase users $usr")
          val data = Json.obj(
            "token" -> token,
            "groupIds" -> List[String](),
            "roles" -> Set[String](),
            "permissions" -> Set(usr.permissions)
          )
          ws.url(conf.get[String]("app.auth.create.firebase.token"))
            .post(data)
            .map(_.body)
            .map(Json.parse)
            .map {
              response =>
                Logger.info(s"response for insertion of user in firebase $response")
                val code = (response \ "code").as[Int]
                if (code == 0) {
                  val redirectionURL = conf.get[String]("app.auth.register.redirect")
                  Logger.info(s"redirecting to home url $redirectionURL")
                  Redirect(redirectionURL)
                }
                else {
                  Redirect(conf.get[String]("app.user.unauthorised"))
                }
            }
        case _ => Future(Redirect(conf.get[String]("app.user.unauthorised")))
      }
  }
  
  def unauthorized = Action {
    implicit request: Request[AnyContent] =>
      Ok("unauthorized")
  }
  
  import scala.concurrent.duration._
  
  def authHandler = Action.async {
    implicit request: Request[AnyContent] ⇒
      request.cookies.foreach(cookie => Logger.debug(s"request id ${request.id} cookie ${cookie.name}=${cookie.value}"))
      request.headers.toMap.foreach { case (key, value) => Logger.debug(s"request id ${request.id} header $key=$value") }
      val r = request.getQueryString("r").getOrElse("")
      val id = request.getQueryString("id").getOrElse("")
      val token = request.getQueryString("token").get
      Logger.debug(s"token $token r $r ")
      val data = Json.obj("token" -> token)
      val redirectUrl = s"$r&token=$token"
      Logger.info(s"redirection of url $redirectUrl")
      Logger.info(s"1.request data for getting user details with token $data")
      ws.url(conf.get[String]("app.auth.user_details")).post(data)
        .map(_.body)
        .map(Json.parse)
        .map {
          response =>
            println(s"response from getting user details by token $response")
            val code = (response \ "code").as[Int]
            if (code == 0) {
              val user = (response \ "value").as[UserG]
              println(s"user found from get details by token $user")
              if (user.roles.contains(Role("group.default")) && user.permissions.contains(Permission("group.default"))) {
                Logger.debug(s"user $user")
                println(s"user $user")
                Logger.info(s"host ${request.host}")
                Redirect(redirectUrl)
                  .withCookies(createCookie(ii_last_login_date, LocalDateTime.now().toString))
                  .withCookies(createCookie(ii_auth_token, token))
              } else {
                Logger.warn(s"user does not have base admin role and permission $user")
                addBasicPermissions(token)
                Redirect(redirectUrl)
                  .withCookies(createCookie(ii_last_login_date, LocalDateTime.now().toString))
                  .withCookies(createCookie(ii_auth_token, token))
              }
            }
            else if (code == 33) {
              Logger.info("code 3 ")
              println("code 3  ")
              addBasicPermissions(token)
              Redirect(redirectUrl)
                .withCookies(createCookie(ii_last_login_date, LocalDateTime.now().toString))
                .withCookies(createCookie(ii_auth_token, token))
            }
            else {
              Logger.info("no data found for token")
              Redirect(conf.get[String]("app.user.unauthorised"))
            }
          
        }.recover {
        case ex: Exception =>
          println(s"exception $ex")
          Redirect(conf.get[String]("app.user.unauthorised"))
      }
  }
  
  private def addBasicPermissions(token: String) = {
    val data = Json.obj(
      "token" -> token,
      "groupIds" -> List[String](),
      "roles" -> Set("group.default"),
      "permissions" -> Set("group.default")
    )
    Await.result(ws.url(conf.get[String]("app.auth.firebase.token.user.add"))
      .post(data)
      .map(_.body)
      .map(Json.parse)
      .map {
        response => Logger.info(s"response for insertion of user in firebase $response")
      }, 3.second)
  }
  
  def createCookie(key: String, value: String)(implicit request: Request[AnyContent]): Cookie = {
    val domain = Some(findDomain(request))
    println("domain for creation of ")
    Cookie(key, value, httpOnly = false, maxAge = Some(Int.MaxValue), domain = domain)
  }
  
  def findDomain(request: Request[AnyContent]): String = {
    if (request.host.contains("localhost")) {
      "localhost"
    } else if (request.host.contains("insuranceinbox.com")) {
      ".insuranceinbox.com"
    } else if (request.host.contains("insuranceinbox.in")) {
      ".insuranceinbox.in"
    } else if (request.host.contains("insuranceinbox.co.in")) {
      ".insuranceinbox.co.in"
    } else {
      ".insuranceinbox.com"
    }
  }
  
  
  def sendSMS(request: JsValue) = {
    ws.url(conf.get[String]("nifi.url")).post(request)
  }

  def urlShortener(url: String): String = {
    val d = Json.obj("longUrl" -> url)
    val response = ws.url(conf.get[String]("google_url_shortening")).post(d)
    val usersList = Await.result(response, Duration.Inf)
    val urlShortened = usersList.json.validate[URL_shortened].getOrElse(URL_shortened("", ""))
    urlShortened.id
  }
  
}
