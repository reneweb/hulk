package hulk.documentation.swagger

import hulk.http.{Action, Ok}
import play.api.libs.json.JsValue

/**
  * Created by reweber on 29/01/2016
  */
class SwaggerController(json: JsValue) {

  def get = Action { request =>
    Ok(json)
  }
}
