package hulk.documentation.swagger

import akka.http.scaladsl.model.HttpMethod
import hulk.documentation.ApiDocumentation
import hulk.documentation.swagger.ParameterDocumentation.ParameterDocumentationWrites
import hulk.documentation.swagger.ResponseDocumentation.ResponseDocumentationWrites
import play.api.libs.json.{Json, JsValue, Writes, JsObject}

/**
  * Created by reweber on 31/01/2016
  */
trait SwaggerRouteDocumentation {
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

object SwaggerRouteDocumentation {
  implicit object SwaggerResourceEndpointWrites extends Writes[ApiDocumentation with SwaggerRouteDocumentation] {
    override def writes(swaggerResourceEndpoint: ApiDocumentation with SwaggerRouteDocumentation): JsValue = {
      val responseDocJson =
        if(swaggerResourceEndpoint.response.nonEmpty) {
          Json.obj("responses" -> swaggerResourceEndpoint.response.foldLeft(Json.obj()) { case (json, responseDoc) =>
            json ++ ResponseDocumentationWrites.writes(responseDoc).as[JsObject]
          })
        } else {
          Json.obj()
        }

      val parameterDocJson = if(swaggerResourceEndpoint.params.nonEmpty) {
        Json.obj("parameters" -> swaggerResourceEndpoint.params.map(ParameterDocumentationWrites.writes))
      } else {
        Json.obj()
      }

      Json.obj(swaggerResourceEndpoint.path ->
        Json.obj(swaggerResourceEndpoint.method.name.toLowerCase -> {
          parameterDocJson ++ responseDocJson ++ swaggerResourceEndpoint.extendedData.getOrElse(Json.obj())
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
