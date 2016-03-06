/*
Spot is a bot, implementing a subset of AIML, and some extensions.
Copyright (C) 2016  Marius Feteanu

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import org.scalatest._
import org.scalatest.prop.TableDrivenPropertyChecks._

import com.spotai.pattern.{Pattern, WildStar, WildUnder, PatternWord}
import com.spotai.pattern.state.PatternContext
import com.spotai.Bot

class PatternSpec extends FlatSpec with Matchers {
  def getMatches(patternString:String, question:String) = {
    val pattern = new Pattern(patternString)
    var patternContext = PatternContext("")
    pattern.matches(Bot.split(question), patternContext)
  }
  val allWildcards = Table(("wild"), ("*"), ("_"))
  /* ------------------------------------------- */
  behavior of "An empty Pattern (from empty list)."
  it must "have no elements" in {
    val pattern = Pattern(Nil)
    pattern.patternElements.size shouldBe 0
  }

  /* --------------------------------------------- */
  behavior of "An empty Pattern (from empty string)."
  it must "have no elements" in {
    val pattern = new Pattern("")
    pattern.patternElements.size shouldBe 0
  }

  /* -------------------------------- */
  behavior of "A Pattern (from string)."
  it must "parse * to a list of one WildStar element." in {
    val pattern = new Pattern("*")
    pattern.patternElements.size shouldBe 1
    pattern.patternElements(0) shouldBe WildStar()
  }
  it must "parse _ to a list of one WildUnder element." in {
    val pattern = new Pattern("_")
    pattern.patternElements.size shouldBe 1
    pattern.patternElements(0) shouldBe WildUnder()
  }
  it must "parse word XYZ to a list of one PatternWord element containing just XYZ." in {
    val pattern = new Pattern("XYZ")
    pattern.patternElements.size shouldBe 1
    pattern.patternElements(0) shouldBe PatternWord("XYZ")
  }
  it must "parse a string of words into a list of identical PatternWords." in {
    val pattern = new Pattern("XX YY ZZ")
    pattern.patternElements.size shouldBe 3
    pattern.patternElements shouldBe List(
      PatternWord("XX"),
      PatternWord("YY"),
      PatternWord("ZZ")
    )
  }
  it must "parse a complex string into a list of identical PatternWords." in {
    val pattern = new Pattern("XX YY * _ ZZ")
    pattern.patternElements.size shouldBe 5
    pattern.patternElements shouldBe List(
      PatternWord("XX"),
      PatternWord("YY"),
      WildStar(),
      WildUnder(),
      PatternWord("ZZ")
    )
  }
  /* Case insensitivity */
  it must "be case insensitive in a complex string when building the pattern." in {
    val pattern = new Pattern("Xx yy * _ ZZ")
    pattern.patternElements.size shouldBe 5
    pattern.patternElements shouldBe List(
      PatternWord("XX"),
      PatternWord("YY"),
      WildStar(),
      WildUnder(),
      PatternWord("ZZ")
    )
  }

  /* --------------------------------------- */
  behavior of "The pattern: '' (empty pattern)"
  it must "match empty string" in {
    getMatches("", "") should not be empty
  }
  it must "not match an actual sentence" in {
    getMatches("", "ABC DEF") shouldBe empty
  }

  /* -------------------------- */
  behavior of "The pattern: 'XYZ'"
  it must "not match an empty string." in {
    getMatches("XYZ", "") shouldBe empty
  }
  it must "match that exact word." in {
    getMatches("XYZ", "XYZ") should not be empty
  }
  it must "not match another word." in {
    getMatches("XYZ", "ABC") shouldBe empty
  }
  it must "not match a sentence ending with XYZ." in {
    getMatches("XYZ", "ABC DEF XYZ") shouldBe empty
  }
  it must "not match a sentence begining with XYZ." in {
    getMatches("XYZ", "XYZ ABC DEF") shouldBe empty
  }
  it must "not match a sentence containing XYZ." in {
    getMatches("XYZ", "ABC XYZ DEF") shouldBe empty
  }

  /* ------------------------------- */
  behavior of "The pattern: (wildcard)"
  it must "not match an empty string." in {
    forAll(allWildcards) { (wildcard:String) =>
      getMatches(s"$wildcard", "") shouldBe empty
    }
  }
  it must "match a word (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      getMatches(s"$wildcard", "XYZ") should not be empty
      getMatches(s"$wildcard", "XYZ").get.star shouldBe "XYZ"
    }
  }
  it must "match a sentence (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      getMatches(s"$wildcard", "ABC DEF GHI") should not be empty
      getMatches(s"$wildcard", "ABC DEF GHI").get.star shouldBe "ABC DEF GHI"
    }
  }

  /* -------------------------------------------------- */
  val prePatternFun = (wildcard:String) => s"$wildcard XYZ"
  behavior of s"The pattern: '(wildcard) XYZ'"
  it must "not match an empty string." in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "") shouldBe empty
    }
  }
  it must "not match the single word XYZ." in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "XYZ") shouldBe empty
    }
  }
  it must "not match some other word." in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "ABC") shouldBe empty
    }
  }
  it must "not match a word preceded by XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "XYZ ABC") shouldBe empty
    }
  }
  it must "not match a sentence containing XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "ABC XYZ DEF") shouldBe empty
    }
  }
  it must "match a word followed by XYZ (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "ABC XYZ") should not be empty
      getMatches(s"$prePattern", "ABC XYZ").get.star shouldBe "ABC"
    }
  }
  it must "match a sentence followed by XYZ (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      val prePattern = prePatternFun(wildcard)
      getMatches(s"$prePattern", "ABC DEF XYZ") should not be empty
      getMatches(s"$prePattern", "ABC DEF XYZ").get.star shouldBe "ABC DEF"
    }
  }

  /* ----------------------- */
  val postPatternFun = (wildcard:String) => s"XYZ $wildcard"
  behavior of s"The pattern: 'XYZ (wildcard)'"
  it must "not match an empty string." in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "") shouldBe empty
    }
  }
  it must "not match the single word XYZ." in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "XYZ") shouldBe empty
    }
  }
  it must "not match some other word." in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "ABC") shouldBe empty
    }
  }
  it must "match a word preceded by XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "XYZ ABC") should not be empty
      getMatches(s"$postPattern", "XYZ ABC").get.star shouldBe "ABC"
    }
  }
  it must "not match a sentence containing XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "ABC XYZ DEF") shouldBe empty
    }
  }
  it must "not match a word followed by XYZ (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "ABC XYZ") shouldBe empty
    }
  }
  it must "match a sentence preceded by XYZ (we assume it matches any)." in {
    forAll(allWildcards) { (wildcard:String) =>
      val postPattern = postPatternFun(wildcard)
      getMatches(s"$postPattern", "XYZ ABC DEF") should not be empty
      getMatches(s"$postPattern", "XYZ ABC DEF").get.star shouldBe "ABC DEF"
    }
  }

  /* --------------------------------------------------0--------- */
  val containsPatternFun = (wildcard:String) => s"UVW $wildcard XYZ"
  behavior of s"The pattern: 'UVW (wildcard) XYZ'"
  it must "not match an empty string." in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "") shouldBe empty
    }
  }
  /* Words not matching */
  it must "not match the single word XYZ." in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "XYZ") shouldBe empty
    }
  }
  it must "not match the single word UVW." in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW") shouldBe empty
    }
  }
  it must "not match some other word." in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC") shouldBe empty
    }
  }
  it must "not match a word preceded by XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "XYZ ABC") shouldBe empty
    }
  }
  it must "not match a word preceded by UVW" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW ABC") shouldBe empty
    }
  }
  it must "not match a word followed by XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC XYZ") shouldBe empty
    }
  }
  it must "not match a word followed by UVW" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC UVW") shouldBe empty
    }
  }

  /* Sentence not matching */
  it must "not match a sentence containing XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC XYZ DEF") shouldBe empty
    }
  }
  it must "not match a sentence containing UVW" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC UVW DEF") shouldBe empty
    }
  }

  it must "not match a sentence ending in XYZ (and not starting with UVW)" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC DEF XYZ") shouldBe empty
    }
  }
  it must "not match a sentence ending in UVW" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "ABC DEF UVW") shouldBe empty
    }
  }

  it must "not match a sentence starting with XYZ" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "XYZ ABC DEF") shouldBe empty
    }
  }
  it must "not match a sentence starting with UVW (and not ending in XYZ)" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW ABC DEF") shouldBe empty
    }
  }
  it must "not match a complex sentence with a different prefix" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "DEF UVW ABC XYZ") shouldBe empty
    }
  }
  it must "not match a complex sentence with a different suffix" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW ABC XYZ DEF") shouldBe empty
    }
  }
  it must "not match a complex sentence with different suffix and prefix" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "GHI UVW ABC XYZ DEF") shouldBe empty
    }
  }

  /* Word matching */
  it must "match a simple sentence 'UVW ABC XYZ'" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW ABC XYZ") should not be empty
    }
  }

  /* Sentence matching */
  it must "match a complex sentence 'UVW ABC DEF XYZ'" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "UVW ABC DEF XYZ") should not be empty
    }
  }
  it must "match a complex sentence 'uvw abc def xyz' (lower case)" in {
    forAll(allWildcards) { (wildcard:String) =>
      val containsPattern = containsPatternFun(wildcard)
      getMatches(s"$containsPattern", "uvw abc def xyz") should not be empty
    }
  }
}
