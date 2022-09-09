package models.insee.etablissement

sealed trait DisclosedStatus

object DisclosedStatus {
  case object P extends DisclosedStatus
  case object N extends DisclosedStatus
}
