package models.insee

package object etablissement {

  implicit class OptionOps(opt: Option[String]) {
    def withTrimmedNonEmpty: Option[String] = opt
      .filter(_.nonEmpty)
      .map(_.trim)
  }

}
