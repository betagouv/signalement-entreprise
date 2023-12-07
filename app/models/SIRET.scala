package models

import controllers.error.ApiError.MalformedSiret
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

case class SIRET private (value: String) extends AnyVal {
  override def toString = value
}

object SIRET {

  val logger: Logger = Logger(this.getClass)
  def pattern        = s"[0-9]{$SiretLength}"

  val SiretLength = 14

  def apply(value: String): Option[SIRET] = {
    val trimmed = value.replaceAll("\\s", "")

    if (isValid(trimmed)) {
      Some(new SIRET(trimmed))
    } else {
      None
    }
  }

  def fromUnsafe(value: String): SIRET =
    SIRET(value).getOrElse(throw MalformedSiret(value))

  def isValid(siret: String): Boolean = siret.matches(SIRET.pattern)

  implicit val SiretColumnType: JdbcType[SIRET] with BaseTypedType[SIRET] = MappedColumnType.base[SIRET, String](
    _.value,
    str =>
      SIRET(str).getOrElse {
        logger.error(
          s"Found malformed Siret $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiret(str)
      }
  )
  implicit val SiretListColumnType: JdbcType[List[SIRET]] with BaseTypedType[List[SIRET]] =
    MappedColumnType.base[List[SIRET], List[String]](
      _.map(_.value),
      _.map(str =>
        SIRET(str).getOrElse {
          logger.error(
            s"Found malformed Siret $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
          )
          // Allowing to create the wrong instance
          throw MalformedSiret(str)
        }
      )
    )
  implicit val SiretWrites: Writes[SIRET] = (o: SIRET) => JsString(o.value)
  implicit val SiretReads: Reads[SIRET]   = (json: JsValue) => json.validate[String].map(SIRET.fromUnsafe)
}
