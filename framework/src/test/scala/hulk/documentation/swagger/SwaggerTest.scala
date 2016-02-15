package hulk.documentation.swagger

import akka.http.scaladsl.model.{HttpMethods, HttpMethod}
import hulk.documentation.ApiDocumentation
import org.specs2.mutable.Specification
import play.api.libs.json.Json

import scala.concurrent.Await

/**
  * Created by reweber on 03/02/2016
  */
class SwaggerTest extends Specification {


  val baseDoc = new ApiDocumentation with SwaggerBaseDocumentation {
    override val host: String = "MyHost"
    override val name: String = "Some Name"
    override val description: String = "Some Description"
    override val apiVersion: String = "1.1"
  }

  val baseDocWithProducerConsumer = new ApiDocumentation with SwaggerBaseDocumentation {
    override val host: String = "MyHost"
    override val name: String = "Some Name"
    override val description: String = "Some Description"
    override val apiVersion: String = "1.1"
    override val produces: Seq[String] = Seq("application/json")
    override val consumes: Seq[String] = Seq("application/json")
  }

  val singleEndpoint = Seq(new ApiDocumentation with SwaggerRouteDocumentation {
    override val name: String = "Some Name"
    override val description: String = "Some Description"
    override val method: HttpMethod = HttpMethods.GET
    override val response: Seq[ResponseDocumentation] = Seq(ResponseDocumentation(200))
    override val path: String = "/my/path"
    override val params: Seq[ParameterDocumentation] = Seq(QueryParameterDocumentation("userId"))
  })

  val singleEndpointWithOptionalFields = Seq(new ApiDocumentation with SwaggerRouteDocumentation {
    override val name: String = "Some Name"
    override val description: String = "Some Description"
    override val method: HttpMethod = HttpMethods.GET
    override val response: Seq[ResponseDocumentation] = Seq(ResponseDocumentation(200, Some("description"), Some("responseType")))
    override val path: String = "/my/path"
    override val params: Seq[ParameterDocumentation] = Seq(
      QueryParameterDocumentation("userId", Some("description"), false, Some("dataType")),
      PathParameterDocumentation("pathParam", Some("description"), false, Some("dataType")))
  })

  val multipleEndpoint = Seq(new ApiDocumentation with SwaggerRouteDocumentation {
    override val name: String = "Some Name"
    override val description: String = "Some Description"
    override val method: HttpMethod = HttpMethods.GET
    override val response: Seq[ResponseDocumentation] = Seq(ResponseDocumentation(200))
    override val path: String = "/my/path"
    override val params: Seq[ParameterDocumentation] = Seq(QueryParameterDocumentation("userId"))
  }, new ApiDocumentation with SwaggerRouteDocumentation {
    override val name: String = "Other Name"
    override val description: String = "Other Description"
    override val method: HttpMethod = HttpMethods.PATCH
    override val response: Seq[ResponseDocumentation] = Seq()
    override val path: String = "/my/path/other"
    override val params: Seq[ParameterDocumentation] = Seq()
  })

  "Swagger#asJson" should {
    "return base swagger json if only passing swagger base" >> {
      val swagger = new Swagger(baseDoc, Seq())
      val json = swagger.asJson

      json must equalTo(Json.parse(
        """
          |{"swagger":"2.0",
          | "info":{"title":"Some Name","description":"Some Description","version":"1.1"},
          | "host":"MyHost","schemes":["http"],
          | "paths":{}}
        """.stripMargin))
    }

    "return base swagger json with producer/consumer if only passing swagger base including producer/consumer fields" >> {
      val swagger = new Swagger(baseDocWithProducerConsumer, Seq())
      val json = swagger.asJson

      json must equalTo(Json.parse(
        """
          |{"swagger":"2.0",
          | "info":{"title":"Some Name","description":"Some Description","version":"1.1"},
          | "host":"MyHost","schemes":["http"],
          | "consumes":["application/json"],
          | "produces":["application/json"],
          | "paths":{}}
        """.stripMargin))
    }

    "return swagger json for single endpoint if only passing one swagger endpoint" >> {
      val swagger = new Swagger(baseDocWithProducerConsumer, singleEndpoint)
      val json = swagger.asJson

      json must equalTo(Json.parse(
        """
          |{"swagger":"2.0",
          | "info":{"title":"Some Name","description":"Some Description","version":"1.1"},
          | "host":"MyHost","schemes":["http"],
          | "consumes":["application/json"],
          | "produces":["application/json"],
          | "paths":{"/my/path":{"get":{"parameters":[{"name":"userId","in":"query","required":true}],"responses":{"200":{}}}}}
          |}
        """.stripMargin))
    }

    "return swagger json for single endpoint with optional data if only passing one swagger endpoint including optional data" >> {
      val swagger = new Swagger(baseDocWithProducerConsumer, singleEndpointWithOptionalFields)
      val json = swagger.asJson

      json must equalTo(Json.parse(
        """
          |{"swagger":"2.0",
          | "info":{"title":"Some Name","description":"Some Description","version":"1.1"},
          | "host":"MyHost","schemes":["http"],
          | "consumes":["application/json"],
          | "produces":["application/json"],
          | "paths":{
          |     "/my/path":{
          |         "get":{
          |             "parameters":[
          |                 {"name":"userId","in":"query","required":false,"description":"description","type":"dataType"},
          |                 {"name":"pathParam","in":"path","required":false,"description":"description","type":"dataType"}
          |             ],
          |             "responses":{"200":{"description":"description","type":"responseType"}}
          |         }
          |     }
          | }
          |}
        """.stripMargin))
    }

    "return swagger json for multiple endpoints if multiple ones are passed" >> {
      val swagger = new Swagger(baseDocWithProducerConsumer, multipleEndpoint)
      val json = swagger.asJson

      json must equalTo(Json.parse(
        """
          |{"swagger":"2.0",
          | "info":{"title":"Some Name","description":"Some Description","version":"1.1"},
          | "host":"MyHost","schemes":["http"],
          | "consumes":["application/json"],
          | "produces":["application/json"],
          | "paths":{
          |    "/my/path":{"get":{"parameters":[{"name":"userId","in":"query","required":true}],"responses":{"200":{}}}},
          |    "/my/path/other":{"patch":{}}}
          |}
        """.stripMargin))
    }
  }

  "Swagger#asController" should {
    "return swagger controller" >> {

      val swagger = new Swagger(baseDoc, Seq.empty)
      swagger.asController must haveClass[SwaggerController]
    }
  }
}
