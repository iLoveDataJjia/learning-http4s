package com.ilovedatajjia
package api.helpers

import java.sql.Timestamp
import sttp.tapir.Schema

/**
 * [[sttp.tapir]] utils.
 */
object TapirUtils {

  // Schema
  implicit val timestampSch: Schema[Timestamp] = Schema.string

}