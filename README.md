#Hulk#

[![Build Status](https://travis-ci.org/reneweb/hulk.svg?branch=master)](https://travis-ci.org/reneweb/hulk)
[![Coverage Status](https://coveralls.io/repos/github/reneweb/hulk/badge.svg?branch=master)](https://coveralls.io/github/reneweb/hulk?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/2c4b5372d0874498b5f86a405b5089bb)](https://www.codacy.com/app/weber-rene/hulk)
[ ![Download](https://api.bintray.com/packages/reneweb/maven/hulk/images/download.svg) ](https://bintray.com/reneweb/maven/hulk/_latestVersion)

Hulk is a web framework to create RESTful API's. It is built on top of Akka HTTP.

Features include:

- Basics (Routing, Filtering, Controller Actions)
- Simple templating engine (using Mustache)
- WebSocket support
- Rate limiting
- Versioning
- Swagger
- Built in metrics
- OAuth server

## Getting started ##

### Set up dependency ###

Hulk is distributed via jcenter. Make sure that it is available as a resolver in your build.
Then add the dependency to your build:

```scala
libraryDependencies ++= Seq ("io.github.reneweb" % "hulk-framework_2.11" % "0.3.1")
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
