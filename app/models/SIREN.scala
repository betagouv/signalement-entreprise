package models

import controllers.error.ApiError.MalformedSiren
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

case class SIREN private (value: String) extends AnyVal {
  override def toString = value
}

object SIREN {

  val logger: Logger = Logger(this.getClass)
  val SirenLength    = 9

  def apply(value: String): Option[SIREN] = {
    val trimmed = value.replaceAll("\\s", "")
    if (isValid(trimmed)) {
      Some(new SIREN(trimmed))
    } else {
      None
    }
  }

  private val tvaNumberPattern = s"FR[0-9]{2}([0-9]{$SirenLength})".r

  def fromTVANumber(value: String): Option[SIREN] = {
    val trimmed = value.replaceAll("\\s", "")
    trimmed match {
      case tvaNumberPattern(siren) => Some(new SIREN(siren))
      case _                       => None
    }
  }

  def apply(siret: SIRET): SIREN = SIREN.fromUnsafe(siret.value.substring(0, SirenLength))

  def fromUnsafe(value: String): SIREN =
    SIREN(value).getOrElse(throw MalformedSiren(value))

  def pattern = s"[0-9]{$SirenLength}"

  def isValid(siren: String): Boolean = siren.matches(SIREN.pattern)

  implicit val SirenColumnType: JdbcType[SIREN] with BaseTypedType[SIREN] = MappedColumnType.base[SIREN, String](
    _.value,
    str =>
      SIREN(str).getOrElse {
        logger.error(
          s"Found malformed SIREN $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiren(str)
      }
  )

  implicit val SirenListColumnType: JdbcType[List[SIREN]] with BaseTypedType[List[SIREN]] =
    MappedColumnType.base[List[SIREN], List[String]](
      _.map(_.value),
      _.map(str =>
        SIREN(str).getOrElse {
          logger.error(
            s"Found malformed SIREN $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
          )
          // Allowing to create the wrong instance
          throw MalformedSiren(str)
        }
      )
    )
  implicit val SirenWrites: Writes[SIREN] = (o: SIREN) => JsString(o.value)
  implicit val SirenReads: Reads[SIREN]   = (json: JsValue) => json.validate[String].map(SIREN.fromUnsafe)
}
