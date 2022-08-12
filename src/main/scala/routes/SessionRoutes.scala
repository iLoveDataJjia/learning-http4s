package com.ilovedatajjia
package routes

import cats.effect.IO
import cats.implicits._
import controllers.SessionController
import models.Session
import org.http4s._
import org.http4s.dsl.io._
import routes.utils.Auth.withAuth

/**
 * Routes related to sessions management.
 */
object SessionRoutes {

  // Define session creation route
  private val sessionCreationRoute: HttpRoutes[IO] = HttpRoutes.of[IO] { case POST -> Root / "create" =>
    SessionController.createSession.redeemWith(
      (e: Throwable) => InternalServerError(e.toString),
      (authToken: String) => Ok(authToken)
    )
  }

  // Define routes
  private val otherRoutes: AuthedRoutes[Session, IO] = AuthedRoutes.of {
    case GET -> Root / "status" as session    => Ok(session)
    case DELETE -> Root / "delete" as session => Ok(s"Hello deleted")
    case GET -> Root / "counts" as session    => Ok(s"Hello counted")
  }

  // Merge all routes
  val routes: HttpRoutes[IO] = sessionCreationRoute <+> withAuth(otherRoutes)

}
