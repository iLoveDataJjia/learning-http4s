package com.ilovedatajjia

import api.services.UserSvc
import cats.effect.IO
import cats.effect.IOApp
import config.AppServer
import config.SparkServer

/**
 * Application configurations & entrypoint.
 */
object Main extends IOApp.Simple {

  /**
   * Run all the required requisites.
   */
  override def run: IO[Unit] = AppServer.run

}
