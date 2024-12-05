package models

import org.specs2.mutable.Specification

class SIRENTest extends Specification {

  "SIREN" should {
    "extract from TVA" in {
      val res = SIREN.fromTVANumber("FR32123456789")

      res.map(_.value) should beSome("123456789")
    }

    "extract from TVA even with spaces" in {
      val res = SIREN.fromTVANumber("  F R3  21 23456 789   ")

      res.map(_.value) should beSome("123456789")
    }
    "not extract from TVA if invalid" in {
      val res = SIREN.fromTVANumber("FR3123456789")

      res should beNone
    }
  }

}
