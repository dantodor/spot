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
import org.scalatest.FlatSpec

import com.spotai.pattern.{Pattern, WildStar, WildUnder, PatternWord}
import com.spotai.pattern.state.PatternContext

class PatternSpec extends FlatSpec{
  /* ------------------------------------------- */
  behavior of "An empty Pattern (from empty list)."
  it must "have no elements" in {
    val pattern = Pattern(Nil)
    assert(pattern.patternElements.size == 0)
  }

  /* --------------------------------------------- */
  behavior of "An empty Pattern (from empty string)."
  it must "have no elements" in {
    val pattern = new Pattern("")
    assert(pattern.patternElements.size == 0)
  }

  /* -------------------------------- */
  behavior of "A Pattern (from string)."
  it must "parse * to a list of one WildStar element." in {
    val pattern = new Pattern("*")
    assert(pattern.patternElements.size == 1)
    assert(pattern.patternElements(0) == WildStar())
  }
  it must "parse _ to a list of one WildUnder element." in {
    val pattern = new Pattern("_")
    assert(pattern.patternElements.size == 1)
    assert(pattern.patternElements(0) == WildUnder())
  }
  it must "parse word XYZ to a list of one PatternWord element containing just XYZ." in {
    val pattern = new Pattern("XYZ")
    assert(pattern.patternElements.size == 1)
    assert(pattern.patternElements(0) == PatternWord("XYZ"))
  }
  it must "parse a string of words into a list of identical PatternWords." in {
    val pattern = new Pattern("XX YY ZZ")
    assert(pattern.patternElements.size == 3)
    assert(pattern.patternElements == List(
      PatternWord("XX"),
      PatternWord("YY"),
      PatternWord("ZZ")
    ))
  }
  it must "parse a complex string into a list of identical PatternWords." in {
    val pattern = new Pattern("XX YY * _ ZZ")
    assert(pattern.patternElements.size == 5)
    assert(pattern.patternElements == List(
      PatternWord("XX"),
      PatternWord("YY"),
      WildStar(),
      WildUnder(),
      PatternWord("ZZ")
    ))
  }

  def getMatches(patternString:String, question:String) = {
    val pattern = new Pattern(patternString)
    var patternContext = PatternContext("")
    pattern.matches(question.split(" "), patternContext)
  }

  /* --------------------------------------- */
  behavior of "The pattern: '' (empty pattern)"
  it must "not match empty string" in {
    assert(!getMatches("", "").isDefined)
  }
  it must "not match an actual sentence" in {
    assert(!getMatches("", "ABC DEF").isDefined)
  }

  /* -------------------------- */
  behavior of "The pattern: 'XYZ'"
  it must "match that exact word." in {
    assert(getMatches("XYZ", "XYZ").isDefined)
  }
  it must "not match another word." in {
    assert(!getMatches("XYZ", "ABC").isDefined)
  }
  it must "not match a sentence ending with XYZ." in {
    assert(!getMatches("XYZ", "ABC DEF XYZ").isDefined)
  }
  it must "not match a sentence begining with XYZ." in {
    assert(!getMatches("XYZ", "XYZ ABC DEF").isDefined)
  }
  it must "not match a sentence containing XYZ." in {
    assert(!getMatches("XYZ", "ABC XYZ DEF").isDefined)
  }
  it must "not match an empty string." in {
    assert(!getMatches("XYZ", "").isDefined)
  }
}