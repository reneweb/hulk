package hulk.documentation

import hulk.documentation.SwaggerBase.SwaggerBaseWrites
import hulk.documentation.SwaggerResourceEndpoint.SwaggerResourceEndpointWrites
import play.api.libs.json.{JsObject, JsValue, Json}

/**
  * Created by reweber on 29/01/2016
  */
class Swagger(swaggerBase: ApiDocumentation with SwaggerBase, endpoints: Seq[ApiDocumentation with SwaggerResourceEndpoint]) {

  def asController: SwaggerController = new SwaggerController(asJson)

  def asJson: JsValue = {
    val pathsJson =
      endpoints.map(SwaggerResourceEndpointWrites.writes).map(_.as[JsObject]).foldLeft(Json.obj()) { case (json, endpoint) =>
        json.deepMerge(endpoint)
      }

    SwaggerBaseWrites.writes(swaggerBase).as[JsObject] ++ Json.obj("paths" -> pathsJson)
  }
}
