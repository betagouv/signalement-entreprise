package models.api

import models.insee.CountryCode
import play.api.libs.json.Json
import play.api.libs.json.OWrites

case class Address(
    number: Option[String] = None,
    street: Option[String] = None,
    addressSupplement: Option[String] = None,
    postalCode: Option[String] = None,
    city: Option[String] = None,
    country: Option[CountryCode] = None
) {

  def isDefined: Boolean = List(
    number,
    street,
    addressSupplement,
    postalCode,
    city,
    country
  ).exists(_.isDefined)

  def nonEmpty: Boolean = !isDefined

  private[this] def fullStreet: String = (number.getOrElse("") + " " + street.getOrElse("")).trim()

  private[this] def fullCity: String = (postalCode.getOrElse("") + " " + city.getOrElse("")).trim()

  def toArray: Seq[String] = Seq(
    fullStreet,
    addressSupplement.getOrElse(""),
    fullCity,
    country.map(_.code).getOrElse("")
  ).filter(_ != "")

  override def toString: String = toArray.mkString(" - ")
}

object Address {
  implicit val writes: OWrites[Address] = Json.writes[Address]
}
