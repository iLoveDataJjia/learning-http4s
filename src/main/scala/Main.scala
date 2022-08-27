package com.ilovedatajjia

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import server.AppServer

/**
 * Application entrypoint.
 */
object Main extends IOApp {

  /**
   * Entrypoint of the application.
   * @param args
   *   Arguments
   * @return
   *   Exit code
   */
  override def run(args: List[String]): IO[ExitCode] = AppServer.runServer()

}
