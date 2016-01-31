package hulk.documentation.swagger

import java.net.InetAddress

import hulk.documentation.ApiDocumentation
import play.api.libs.json.{Json, JsValue, Writes, JsObject}

/**
  * Created by reweber on 31/01/2016
  */

trait SwaggerBaseDocumentation {
  self : ApiDocumentation =>

  val host: String = InetAddress.getLocalHost.getHostName
  val swaggerVersion: String = "2.0"
  val apiVersion: String
  val schemes: Seq[String] = Seq("http")
  val basePath: String = "/"
  val consumes: Seq[String] = Seq()
  val produces: Seq[String] = Seq()
  val extendedData: Option[JsObject] = None
}



object SwaggerBaseDocumentation {
  implicit object SwaggerBaseWrites extends Writes[ApiDocumentation with SwaggerBaseDocumentation] {
    override def writes(swaggerBase: ApiDocumentation with SwaggerBaseDocumentation): JsValue = {
      val consumesDocJson = if(swaggerBase.consumes.nonEmpty) {
        Json.obj("consumes" -> swaggerBase.consumes)
      } else {
        Json.obj()
      }

      val producesDocJson = if(swaggerBase.produces.nonEmpty) {
        Json.obj("produces" -> swaggerBase.produces)
      } else {
        Json.obj()
      }

      Json.obj(
        "swagger" -> swaggerBase.swaggerVersion,
        "info" -> Json.obj(
          "title" -> swaggerBase.name,
          "description" -> swaggerBase.description,
          "version" -> swaggerBase.apiVersion
        ),
        "host" -> swaggerBase.host,
        "schemes" -> swaggerBase.schemes
      ) ++ consumesDocJson ++ producesDocJson ++ swaggerBase.extendedData.getOrElse(Json.obj())
    }
  }
}
