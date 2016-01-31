package hulk.documentation.swagger

import hulk.documentation.ApiDocumentation
import hulk.documentation.swagger.SwaggerBaseDocumentation.SwaggerBaseWrites
import hulk.documentation.swagger.SwaggerRouteDocumentation.SwaggerResourceEndpointWrites
import play.api.libs.json.{JsObject, JsValue, Json}

/**
  * Created by reweber on 29/01/2016
  */
class Swagger(swaggerBase: ApiDocumentation with SwaggerBaseDocumentation, endpoints: Seq[ApiDocumentation with SwaggerRouteDocumentation]) {

  def asController: SwaggerController = new SwaggerController(asJson)

  def asJson: JsValue = {
    val pathsJson =
      endpoints.map(SwaggerResourceEndpointWrites.writes).map(_.as[JsObject]).foldLeft(Json.obj()) { case (json, endpoint) =>
        json.deepMerge(endpoint)
      }

    SwaggerBaseWrites.writes(swaggerBase).as[JsObject] ++ Json.obj("paths" -> pathsJson)
  }
}
