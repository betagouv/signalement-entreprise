package models

import controllers.error.ApiError.MalformedSiren
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._

case class SIREN private(value: String) extends AnyVal {
  override def toString = value
}

object SIREN {

  val logger: Logger = Logger(this.getClass)
  val SirenLength = 9

  def apply(value: String): Option[SIREN] = {
    val trimmed = value.replaceAll("\\s", "")
    if (isValid(trimmed)) {
      Some(new SIREN(trimmed))
    } else {
      None
    }
  }

  def apply(siret: SIRET): SIREN = SIREN.fromUnsafe(siret.value.substring(0, SirenLength))

  def fromUnsafe(value: String): SIREN =
    SIREN(value).getOrElse(throw MalformedSiren(value))

  def pattern = s"[0-9]{$SirenLength}"

  def isValid(siren: String): Boolean = siren.replaceAll("\\s", "").matches(SIREN.pattern)

  implicit val SirenColumnType = MappedColumnType.base[SIREN, String](
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

  implicit val SirenListColumnType = MappedColumnType.base[List[SIREN], List[String]](
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
  implicit val SirenWrites = new Writes[SIREN] {
    def writes(o: SIREN): JsValue =
      JsString(o.value)
  }
  implicit val SirenReads = new Reads[SIREN] {
    def reads(json: JsValue): JsResult[SIREN] = json.validate[String].map(SIREN.fromUnsafe)
  }
}
