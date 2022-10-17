package models

import controllers.error.ApiError.MalformedSiren
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._

case class Siren private (value: String) extends AnyVal {
  override def toString = value
}

object Siren {

  val logger: Logger = Logger(this.getClass)
  val SirenLength = 9

  def apply(value: String): Option[Siren] = {
    val trimmed = value.replaceAll("\\s", "")
    if (isValid(trimmed)) {
      Some(new Siren(trimmed))
    } else {
      None
    }
  }

  def apply(siret: Siret): Siren = Siren.fromUnsafe(siret.value.substring(0, SirenLength))

  def fromUnsafe(value: String): Siren =
    Siren(value).getOrElse(throw MalformedSiren(value))

  def pattern = s"[0-9]{$SirenLength}"

  def isValid(siren: String): Boolean = siren.replaceAll("\\s", "").matches(Siren.pattern)

  implicit val SirenColumnType = MappedColumnType.base[Siren, String](
    _.value,
    str =>
      Siren(str).getOrElse {
        logger.error(
          s"Found malformed SIREN $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiren(str)
      }
  )

  implicit val SirenListColumnType = MappedColumnType.base[List[Siren], List[String]](
    _.map(_.value),
    _.map(str =>
      Siren(str).getOrElse {
        logger.error(
          s"Found malformed SIREN $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiren(str)
      }
    )
  )
  implicit val SirenWrites = new Writes[Siren] {
    def writes(o: Siren): JsValue =
      JsString(o.value)
  }
  implicit val SirenReads = new Reads[Siren] {
    def reads(json: JsValue): JsResult[Siren] = json.validate[String].map(Siren.fromUnsafe)
  }
}
