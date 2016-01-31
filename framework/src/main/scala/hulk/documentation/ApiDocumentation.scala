package hulk.documentation

import java.net.InetAddress

import akka.http.scaladsl.model.HttpMethod
import hulk.documentation.ParameterDocumentation.ParameterDocumentationWrites
import hulk.documentation.ResponseDocumentation.ResponseDocumentationWrites
import play.api.libs.json.{Json, JsValue, Writes, JsObject}

/**
  * Created by reweber on 21/01/2016
  */
trait ApiDocumentation {
  val name: String
  val description: String
}

trait SwaggerBase {
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

trait SwaggerResourceEndpoint {
  self : ApiDocumentation =>

  val method: HttpMethod
  val path: String
  val params: Seq[ParameterDocumentation]
  val response: Seq[ResponseDocumentation]
  val extendedData: Option[JsObject] = None
}

trait ParameterDocumentation
case class QueryParameterDocumentation(name: String, description: Option[String] = None, required: Boolean = true, dataType: Option[String] = None, extendedData: Option[JsObject] = None) extends ParameterDocumentation
case class PathParameterDocumentation(name: String, description: Option[String] = None, required: Boolean = true, dataType: Option[String] = None, extendedData: Option[JsObject] = None) extends ParameterDocumentation

case class ResponseDocumentation(statusCode: Int, description: Option[String] = None, responseType: Option[String] = None, extendedData: Option[JsObject] = None)

object SwaggerBase {
  implicit object SwaggerBaseWrites extends Writes[ApiDocumentation with SwaggerBase] {
    override def writes(swaggerBase: ApiDocumentation with SwaggerBase): JsValue = {
      Json.obj(
        "swagger" -> swaggerBase.swaggerVersion,
        "info" -> Json.obj(
          "title" -> swaggerBase.name,
          "description" -> swaggerBase.description,
          "version" -> swaggerBase.apiVersion
        ),
        "consumes" -> swaggerBase.consumes,
        "produces" -> swaggerBase.produces,
        "host" -> swaggerBase.host,
        "schemes" -> swaggerBase.schemes
      ) ++ swaggerBase.extendedData.getOrElse(Json.obj())
    }
  }
}

object SwaggerResourceEndpoint {
  implicit object SwaggerResourceEndpointWrites extends Writes[ApiDocumentation with SwaggerResourceEndpoint] {
    override def writes(swaggerResourceEndpoint: ApiDocumentation with SwaggerResourceEndpoint): JsValue = {
      val responseDocJson =
        swaggerResourceEndpoint.response.foldLeft(Json.obj()) { case (json, responseDoc) =>
          json ++ ResponseDocumentationWrites.writes(responseDoc).as[JsObject]
        }

      Json.obj(swaggerResourceEndpoint.path ->
        Json.obj(swaggerResourceEndpoint.method.name.toLowerCase -> {
          Json.obj(
            "parameters" -> swaggerResourceEndpoint.params.map(ParameterDocumentationWrites.writes),
            "responses" -> responseDocJson
          ) ++ swaggerResourceEndpoint.extendedData.getOrElse(Json.obj())
        })
      )
    }
  }
}

object ParameterDocumentation {
  implicit object ParameterDocumentationWrites extends Writes[ParameterDocumentation] {
    override def writes(parameterDocumentation: ParameterDocumentation): JsValue = {
      parameterDocumentation match {
        case q: QueryParameterDocumentation =>
          Json.obj(
            "name" -> q.name,
            "in" -> "query",
            "required" -> q.required
          ) ++
            q.description.map(d => Json.obj("description" -> d)).getOrElse(Json.obj()) ++
            q.dataType.map(t => Json.obj("type" -> t)).getOrElse(Json.obj()) ++
            q.extendedData.getOrElse(Json.obj())
        case p: PathParameterDocumentation =>
          Json.obj(
            "name" -> p.name,
            "in" -> "path",
            "required" -> p.required
          ) ++
            p.description.map(d => Json.obj("description" -> d)).getOrElse(Json.obj()) ++
            p.dataType.map(t => Json.obj("type" -> t)).getOrElse(Json.obj()) ++
            p.extendedData.getOrElse(Json.obj())
      }
    }
  }
}

object ResponseDocumentation {
  implicit object ResponseDocumentationWrites extends Writes[ResponseDocumentation] {
    override def writes(responseDocumentation: ResponseDocumentation): JsValue = {
      Json.obj(responseDocumentation.statusCode.toString -> {
        responseDocumentation.description.map(d => Json.obj("description" -> d)).getOrElse(Json.obj()) ++
        responseDocumentation.responseType.map(t => Json.obj("type" -> t)).getOrElse(Json.obj()) ++
        responseDocumentation.extendedData.getOrElse(Json.obj())
      })
    }
  }
}