package models.insee.etablissement

import enumeratum._
import repositories.PostgresProfile.api._

sealed abstract class DisclosedStatus(override val entryName: String) extends EnumEntry

object DisclosedStatus extends PlayEnum[DisclosedStatus] {

  val values = findValues
  case object Public extends DisclosedStatus("O")
  case object NonPublic extends DisclosedStatus("N")

  implicit val DisclosedStatusColumnType = MappedColumnType.base[DisclosedStatus, String](
    _.entryName,
    DisclosedStatus.withNameOption(_).getOrElse(DisclosedStatus.Public)
  )
}
