import java.time.LocalDateTime

import java.time.OffsetDateTime
import java.time.ZoneOffset
import scala.util.Try

package object orchestrators {

  def toOffsetDateTime(maybeDate: Option[String]) =
    maybeDate.flatMap(d => Try(OffsetDateTime.of(LocalDateTime.parse(d), ZoneOffset.UTC)).toOption)

}
