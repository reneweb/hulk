#Hulk#

Hulk is a web framework to create RESTful API's. It is built on top of Akka HTTP.

Features include:

- Basics (Routing, Filtering, Controller Actions)
- Simple templating engine (using Mustache)
- Rate limiting
- Versioning
- Swagger
- Built in metrics

Features yet to come:

- HAL/ Json LD support
- Performance Testing support
- OAuth client/server
- Websocket support

## Getting started ##

### Set up dependency ###

Hulk is distributed via jcenter. Make sure that it is available as a resolver.
Then add the dependency to your build:

```scala
libraryDependencies ++= Seq ("io.github.reneweb" % "hulk-framework_2.11" % "0.1.0")
```

### Simple usage example ###

```scala
object Application extends App {

  val action = Action(request => Ok())
  val router = new Router {
    override def router: Map[RouteDef, Action] = Map((HttpMethods.GET, "/test") -> action)
  }

  HulkHttpServer(router).run()
}
```

More examples: https://github.com/reneweb/hulk/tree/master/examples/src/main/scala/hulk
