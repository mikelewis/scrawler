package org.scrawler
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GeneralUtilsSpec extends Specification {
  "genericRegexMatch" should {
    "return true for a given match" in {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13hey432") must beTrue
    }

    "return false for a failed match" in {
      GeneralUtils.genericRegexMatch(Seq("""brains""".r, """hey""".r), "13432") must beFalse
    }
  }
}