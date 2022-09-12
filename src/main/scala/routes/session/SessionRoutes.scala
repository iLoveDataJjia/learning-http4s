package com.ilovedatajjia
package routes.session

import cats.effect.IO
import cats.implicits._
import controllers.SessionController
import org.http4s._
import org.http4s.dsl.io._
import routes.session.entity.SessionStatusEntity
import routes.utils.Auth._
import routes.utils.Response._
import com.ilovedatajjia.models.session.Session

/**
 * Routes related to sessions management.
 */
object SessionRoutes {

  // Define session creation route
  private val sessionCreationRoute: HttpRoutes[IO] = HttpRoutes.of[IO] { case POST -> Root / "create" =>
    SessionController.createSession.toResponse
  }

  // Define retrieve session status, terminate session & list all active sessions routes
  private val otherRoutes: AuthedRoutes[Session, IO] = AuthedRoutes.of {
    case GET -> Root / "status" as session     =>
      Ok(
        SessionStatusEntity(session.id,
                            session.createdAt.toString,
                            session.updatedAt.toString,
                            session.terminatedAt.map(_.toString)))
    case POST -> Root / "terminate" as session =>
      SessionController.terminateSession(session).toResponse
    case GET -> Root / "listing" as session    => SessionController.listSessions(session).toResponse
  }

  // Merge all routes
  val routes: HttpRoutes[IO] = sessionCreationRoute <+> withAuth(otherRoutes) // Always the non-auth routes first

}
