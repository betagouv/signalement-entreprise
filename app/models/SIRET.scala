package models

import controllers.error.ApiError.MalformedSiret
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._

case class SIRET private (value: String) extends AnyVal {
  override def toString = value
}

object SIRET {

  val logger: Logger = Logger(this.getClass)
  def pattern = s"[0-9]{$SiretLength}"

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

  implicit val SiretColumnType = MappedColumnType.base[SIRET, String](
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
  implicit val SiretListColumnType = MappedColumnType.base[List[SIRET], List[String]](
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
  implicit val SiretWrites = new Writes[SIRET] {
    def writes(o: SIRET): JsValue =
      JsString(o.value)
  }
  implicit val SiretReads = new Reads[SIRET] {
    def reads(json: JsValue): JsResult[SIRET] = json.validate[String].map(SIRET.fromUnsafe)
  }
}
