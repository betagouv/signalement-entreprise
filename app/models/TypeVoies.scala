package models

object TypeVoies {
  val values = Seq(
    ("ALL", "Allée"),
    ("AV", "Avenue"),
    ("BD", "Boulevard"),
    ("CAR", "Carrefour"),
    ("CHE", "Chemin"),
    ("CHS", "Chaussée"),
    ("CITE", "Cité"),
    ("COR", "Corniche"),
    ("CRS", "Cours"),
    ("DOM", "Domaine"),
    ("DSC", "Descente"),
    ("ECA", "Ecart"),
    ("ESP", "Esplanade"),
    ("FG", "Faubourg"),
    ("GR", "Grande Rue"),
    ("HAM", "Hameau"),
    ("HLE", "Halle"),
    ("IMP", "Impasse"),
    ("LD", "Lieu dit"),
    ("LOT", "Lotissement"),
    ("MAR", "Marché"),
    ("MTE", "Montée"),
    ("PAS", "Passage"),
    ("PL", "Place"),
    ("PLN", "Plaine"),
    ("PLT", "Plateau"),
    ("PRO", "Promenade"),
    ("PRV", "Parvis"),
    ("QUA", "Quartier"),
    ("QUAI", "Quai"),
    ("RES", "Résidence"),
    ("RLE", "Ruelle"),
    ("ROC", "Rocade"),
    ("RPT", "Rond Point"),
    ("RTE", "Route"),
    ("RUE", "Rue"),
    ("SEN", "Sente - Sentier"),
    ("SQ", "Square"),
    ("TPL", "Terre-plein"),
    ("TRA", "Traverse"),
    ("VLA", "Villa"),
    ("VLGE", "Village")
  )

  def getByShortName(shortName: String): Option[String] =
    values.find(_._1 == shortName).map(_._2.toUpperCase)
}
