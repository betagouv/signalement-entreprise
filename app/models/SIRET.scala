package models

import controllers.error.ApiError.MalformedSIRET
import play.api.libs.json._
import repositories.PostgresProfile.api._

case class SIRET(value: String) {
  override def toString = value
}

object SIRET {

  val length = 14

  def apply(value: String): SIRET =
    if (value.replaceAll("\\s", "").matches(SIRET.pattern)) {
      new SIRET(value)
    } else {
      throw MalformedSIRET(value)
    }

  def pattern = s"[0-9]{$length}"

  def isValid(siret: String) = siret.matches(SIRET.pattern)

  implicit val siretColumnType = MappedColumnType.base[SIRET, String](
    _.value,
    SIRET(_)
  )
  implicit val siretListColumnType = MappedColumnType.base[List[SIRET], List[String]](
    _.map(_.value),
    _.map(SIRET(_))
  )
  implicit val siretWrites = new Writes[SIRET] {
    def writes(o: SIRET): JsValue =
      JsString(o.value)
  }
  implicit val siretReads = new Reads[SIRET] {
    def reads(json: JsValue): JsResult[SIRET] = json.validate[String].map(SIRET(_))
  }
}
