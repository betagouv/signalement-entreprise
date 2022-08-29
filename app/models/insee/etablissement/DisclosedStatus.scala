package models.insee.etablissement

import enumeratum._

sealed trait DisclosedStatus extends EnumEntry

object DisclosedStatus extends PlayEnum[DisclosedStatus] {
  val values = findValues

  case object P extends DisclosedStatus
  case object N extends DisclosedStatus
}
