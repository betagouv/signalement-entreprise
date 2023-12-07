package models.insee.etablissement

import enumeratum._
import repositories.PostgresProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

sealed abstract class DisclosedStatus(override val entryName: String) extends EnumEntry

object DisclosedStatus extends PlayEnum[DisclosedStatus] {

  val values = findValues
  case object Public extends DisclosedStatus("O")
  // Legacy
  // https://sirene.fr/static-resources/htm/v_sommaire.htm#diffusionpartielle
  // https://sirene.fr/static-resources/htm/v_sommaire.htm#72
  case object NonPublicLegacy extends DisclosedStatus("N")
  case object NonPublic       extends DisclosedStatus("P")

  implicit val DisclosedStatusColumnType: JdbcType[DisclosedStatus] with BaseTypedType[DisclosedStatus] =
    MappedColumnType.base[DisclosedStatus, String](
      _.entryName,
      DisclosedStatus.withName
    )
}
