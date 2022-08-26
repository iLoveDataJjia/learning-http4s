package com.ilovedatajjia
package servers

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.Router
import routes.session.SessionRoutes
import routes.session.SessionSwagger

object SwaggerServer {

  // Retrieve all route(s)
  val combinedRoutes: HttpApp[IO] = Router("/session" -> SessionSwagger.routes).orNotFound

  // Build the server
  val serverBuilder: EmberServerBuilder[IO] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"127.0.0.1") // localhost equivalent
    .withPort(port"8081")
    .withHttpApp(combinedRoutes)

  /**
   * Run the HTTP4s server.
   */
  def runServer(): IO[ExitCode] = serverBuilder.build
    .use(_ => IO.never)
    .as(ExitCode.Success)

}
