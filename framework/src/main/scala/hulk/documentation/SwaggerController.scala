package hulk.documentation

import hulk.http.{Ok, Action}
import play.api.libs.json.{JsValue, JsObject}

/**
  * Created by reweber on 29/01/2016
  */
class SwaggerController(json: JsValue) {

  def get = Action { request =>
    Ok(json)
  }
}
