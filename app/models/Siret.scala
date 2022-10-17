package models

import controllers.error.ApiError.MalformedSiret
import play.api.Logger
import play.api.libs.json._
import repositories.PostgresProfile.api._

case class Siret private (value: String) extends AnyVal {
  override def toString = value
}

object Siret {

  val logger: Logger = Logger(this.getClass)
  def pattern = s"[0-9]{$SiretLength}"

  val SiretLength = 14

  def apply(value: String): Option[Siret] = {
    val trimmed = value.replaceAll("\\s", "")

    if (isValid(trimmed)) {
      Some(new Siret(trimmed))
    } else {
      None
    }
  }

  def fromUnsafe(value: String): Siret =
    Siret(value).getOrElse(throw MalformedSiret(value))

  def isValid(siret: String): Boolean = siret.matches(Siret.pattern)

  implicit val SiretColumnType = MappedColumnType.base[Siret, String](
    _.value,
    str =>
      Siret(str).getOrElse {
        logger.error(
          s"Found malformed Siret $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiret(str)
      }
  )
  implicit val SiretListColumnType = MappedColumnType.base[List[Siret], List[String]](
    _.map(_.value),
    _.map(str =>
      Siret(str).getOrElse {
        logger.error(
          s"Found malformed Siret $str in etablissement database, this is either a bug or an INSEE api error and please fix the data"
        )
        // Allowing to create the wrong instance
        throw MalformedSiret(str)
      }
    )
  )
  implicit val SiretWrites = new Writes[Siret] {
    def writes(o: Siret): JsValue =
      JsString(o.value)
  }
  implicit val SiretReads = new Reads[Siret] {
    def reads(json: JsValue): JsResult[Siret] = json.validate[String].map(Siret.fromUnsafe)
  }
}
