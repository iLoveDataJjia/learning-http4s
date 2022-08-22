package com.ilovedatajjia
package routes.utils

import cats.data._
import cats.effect.IO
import cats.implicits._
import controllers.SessionController
import models.Session
import org.http4s._
import org.http4s.AuthScheme
import org.http4s.Credentials
import org.http4s.Request
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware

/**
 * Authorization utils for routes.
 */
object Auth {

  // Retrieve session
  def retrieveUser: Kleisli[IO, String, Session] = Kleisli(authToken => IO(???))

  // Authorization verification policy
  private val authPolicy: Kleisli[IO, Request[IO], Either[String, Session]] = Kleisli({ request =>
    // Check if valid authorization
    val validatedAuth: Either[String, String] = for {
      authorizationHeader <-
        request.headers.get[Authorization].toRight("Could not find OAuth 2.0 `Authorization` header")
      session             <- authorizationHeader.credentials match {
                               case Credentials.Token(AuthScheme.Bearer, authToken) =>
                                 Right(authToken)
                               case Credentials.Token(authSchema, _)                =>
                                 Left(s"Expecting `Bearer` authorization prefix but got `$authSchema`")
                               case x                                               =>
                                 Left(s"Expecting `Token` credentials but got `${x.getClass}` credentials")
                             }
    } yield session

    // Check if valid session
    validatedAuth match {
      case Right(authToken) => SessionController.verifyAuthorization(authToken).red
      case _                => _
    }

    // Prepare session verification (Either[_, IO[Session]] to IO[Either[_, Session]])
    validatedAuth.traverse {
      x => val test = retrieveUser.run(x)
    }
  })

  // Middleware to actual routes
  private val onFailure: AuthedRoutes[String, IO] =
    Kleisli(req => OptionT.liftF(Forbidden(req.context))) // Define the error if failure == Left (here Forbidden == 403)
  val withAuth: AuthMiddleware[IO, Session] = AuthMiddleware(authPolicy, onFailure)

}
